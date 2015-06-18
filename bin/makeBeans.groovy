
import groovy.sql.Sql
import java.util.logging.Logger

def propFilePath = System.getProperty("user.dir") + File.separator + "WEB-INF" + File.separator + "db.properties"

File f = new File(propFilePath)

if (!f.exists()) {
        println "make sure db.properties is in  /<appDir>/WEB-INF"
        println "you must also be running this from the root of your web application"
        System.exit(0)
}

PropertiesFile prop = new PropertiesFile(propFilePath)

db = Sql.newInstance(prop.getString("database.url"),
        prop.getString("database.username"),
        prop.getString("database.password"),
        prop.getString("database.driver"))

def modelPackage = prop.getString("model.package")

def tables = []

if (args.length > 2)
        tables << Table.unCamelCase(args[2])
else
        tables = getTableNames(db.connection)

tables.each {
        def t = new Table(db, it, modelPackage)
        t.writeGroovyBean()
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

def String getAppPath() {
        return System.getProperty("user.dir")
}

def getTableNames(connection) {
        def meta = connection.getMetaData()
        def _types = ['TABLE', 'VIEW']
        def types = (String[]) _types
        def rs = meta.getTables(null, null, '%', types)

        table = []
        while (rs.next()) {
                table << rs.getString("TABLE_NAME")
        }

        return table
}



class Table {
        def db
        String tableName
        String beanName

        String deleteQuery
        String selectQuery
        String updateQuery
        String insertQuery
        String src
        String packageName

        def columns = []
        def primaryKeys = []
        def dir
        def modelDir
        def modelPackage

        Table(db, tableName, modelPackage) {
                this.modelPackage = modelPackage
                this.tableName = tableName
                this.beanName = parseBeanName()
                this.db = db
                this.packageName = parsePackageName()
                this.tableName = parseTableName()
                load()
        }

        def load() {
                packageName = getPackageName()
                beanName = parseBeanName()
                columns = loadColumnNames()
                primaryKeys = loadPrimaryKeys()
                src = createGroovyBean()
        }

        def parsePackageName() {
                def packageName = modelPackage
                modelDir = System.getProperty("user.dir") + File.separator + "WEB-INF" + File.separator + packageName.replace('.', File.separator)
                return packageName
        }

        def parseBeanName() {
                def b = ''
                def tbl = tableName
                if (tableName.indexOf('.') > -1) {
                        tbl = tableName.substring(tableName.lastIndexOf(".") + 1, tableName.length())
                }

                if (tbl.toUpperCase().startsWith('TBL')) {
                        b = tbl.substring(3)
                } else {
                        b = tbl
                }

                return b
        }

        def parseTableName() {
                if (tableName.indexOf('.') > -1) {
                        return tableName.substring(tableName.lastIndexOf(".") + 1, tableName.length())
                } else {
                        return tableName
                }
        }

        def loadColumnNames() {
                def columns = []
                db.query('SELECT * FROM ' + tableName + ' WHERE 1=2') {rs ->
                        def meta = rs.metaData
                        def x = meta.columnCount

                        for (i in 0..<x) {
                                def col = new Column()
                                col.tableColumnName = meta.getColumnName(i + 1).toString().toLowerCase()
                                col.colType = meta.getColumnTypeName(i + 1)

                                col.javaType = getJavaType(col.colType, meta.getPrecision(i + 1))
                                col.propertyName = camelCase(col.tableColumnName)
                                col.primaryKey = isPrimaryKey(db.connection.getMetaData(), col.tableColumnName)
                                columns << col
                        }
                }

                return columns
        }

        def loadPrimaryKeys() {
                def pk = []
                for (col in columns) {
                        if (col.primaryKey)
                                pk += col
                }
                return pk
        }

        def getJavaType(dbType, length) {
                if ((dbType == 'BIT' || dbType == 'TINYINT') && length == 1)
                        return 'boolean'

                switch (dbType.toUpperCase().split(' ')[0]) {

                        case ['VARCHAR', 'CHAR']:
                                return 'String'
                        case ['INTEGER', 'INT', 'SMALLINT', 'MEDIUMINT', 'TINYINT']:
                                return 'Integer'
                        case ['BIGINT']:
                                return 'Long'
                        case 'FLOAT':
                                return 'Float'
                        case 'DOUBLE':
                                return 'Double'
                        case 'DECIMAL':
                                return 'BigDecimal'
                        case 'DOUBLE PRECISION':
                                return 'Float'
                        case 'NUMERIC':
                                return 'Integer'
                        case ['DATE']:
                                return 'Date'
                        case ['TIMESTAMP', 'DATETIME']:
                                return 'Timestamp'
                        case ['TEXT', 'LONGTEXT', 'ENUM', 'MEDIUMTEXT', 'CHAR']:
                                return 'Timestamp'
                        default:
                                if (dbType.toString().startsWith("BLOB"))
                                        return 'String'
                }
        }

        def static String camelCase(String column) {
                StringBuffer newColumn = new StringBuffer()
                boolean underScoreFound = false
                int index = -1
                int currentPosition = 0
                while ((index = column.indexOf('_', currentPosition)) > -1) {
                        newColumn.append(column.substring(currentPosition, index).toLowerCase())
                        newColumn.append(column.substring(index + 1, index + 2).toUpperCase())

                        currentPosition = index + 2
                        underScoreFound = true
                }

                if (underScoreFound == false) {
                        return column
                } else {
                        newColumn.append(column.substring(currentPosition, column.length()).toLowerCase())
                }

                return newColumn.toString()
        }

        def static String unCamelCase(String column) {
                StringBuffer newColumn = new StringBuffer()
                def i = 0
                column.each {
                        if (Character.isLetter(column.charAt(i)) && Character.isUpperCase(column.charAt(i))) {
                                if (i > 0)
                                        newColumn.append("_")

                                newColumn.append(Character.toLowerCase(column.charAt(i)))
                        } else {
                                newColumn.append(column.charAt(i))
                        }
                        i++
                }

                return newColumn.toString()
        }

        def isPrimaryKey(meta, columnName) {
                def rset = meta.getPrimaryKeys(null, null, tableName)
                while (rset.next()) {
                        def idx = rset.getString('COLUMN_NAME').toLowerCase()

                        if (idx.equals(columnName)) {
                                return true
                        }
                }
                return false
        }


        def createGroovyBean() {
                def src = ''

                src = """package $packageName
                import groovy.transform.ToString

                @ToString(includeNames = true, includeFields = true)
                """

                //src += 'import java.util.Date\n\n'
                src += 'import java.sql.*\n\n'
                
                src += 'class ' + upperFirst(camelCase(beanName)) + ' {\n'

                src += '\tprivate def dynamicProperties = []\n'
                src += '\tstatic primaryKeys = ['
                boolean pks = false
                primaryKeys.each {
                        src += "'" + it.propertyName + "', '"
                        pks = true
                }

                if (pks) {
                        src = src.substring(0, src.lastIndexOf(','))
                }
                src += ']\n\n'

                for (col in columns) {
                        src += '\t' + col.javaType + ' ' + col.propertyName + '\n'
                }

                src += '}'

                return src
        }


        def lowerFirst(s) {
                return s.substring(0, 1).toLowerCase() + s.substring(1)
        }

        def upperFirst(s) {
                return s.substring(0, 1).toUpperCase() + s.substring(1)
        }

        def writeGroovyBean() {
                def dir = modelDir + File.separator
                new File(dir).mkdirs()
                def camelCaseBeanName = upperFirst(camelCase(beanName))
                def f = new FileOutputStream(dir + camelCaseBeanName + '.groovy')
                f.withWriter {w ->
                        w << src
                }
                println 'bean created: ' + dir + camelCaseBeanName + '.groovy'
        }
}



class Column {
        String colType
        String tableColumnName
        String propertyName
        String javaType
        boolean primaryKey
}

class PropertiesFile {
        Properties prop
        String fileName

        def PropertiesFile(String fileName) {
                this.fileName = fileName
                load()
        }

        def load() {
                prop = new Properties()
                FileInputStream fis = null
                try {
                        if (fileName.toLowerCase().startsWith("classpath:")) {
                                URL url = getClass().getClassLoader().getResource(fileName.substring(10))
                                fis = new FileInputStream(url.getFile())
                                prop.load(fis)
                        } else {
                                fis = new FileInputStream(new File(fileName))
                                prop.load(fis)
                        }

                } finally {
                        try {
                                fis.close()
                        } catch (IOException e) {
                        }
                }
        }

        def getString(String key) {
                return prop.get(key).toString()
        }

        def getInt(String key) {
                return Integer.parseInt(getString(key))
        }
}
