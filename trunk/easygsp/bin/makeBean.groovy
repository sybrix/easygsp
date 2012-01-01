/**
 * Created by IntelliJ IDEA.
 * User: dsmith
 * Date: Jan 18, 2008
 * Time: 9:38:49 PM
 * To change this template use File | Settings | File Templates.
 */


//database.driver=com.mysql.jdbc.Driver
//database.url=jdbc:mysql://localhost:3306/mysql
//database.password=root
//database.username=root

import groovy.sql.Sql
import java.util.logging.Logger

def propFilePath = System.getProperty("user.dir") + File.separator + "WEB-INF" + File.separator + "db.properties"
File f = new File(propFilePath)
if (!f.exists()){
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


println args

def t = new Table(db, args[2], args[0], modelPackage );
t.load();
//println t.src;
//println t.selectQuery;
//println t.deleteQuery;
//println t.updateQuery;
//println t.updateQuery;

t.writeGroovyBean();





def String getAppPath() {
        return System.getProperty("user.dir");
}


def getTableNames(connection) {
        // defs allow watches in intellij
        def meta = connection.getMetaData()
        def _types = ["TABLE"]
        def types = (String[]) _types
        def rs = meta.getTables(null, null, '%', types);

        table = []
        i = 0;
        while (rs.next()) {
                table[i++] = rs.getString("TABLE_NAME");
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
        Table(db, tableName, dir,modelPackage ) {
        	this.modelPackage = modelPackage
                this.tableName = tableName
                this.dir = dir
                beanName = parseBeanName()
                this.db = db
                packageName = parsePackageName()
                this.tableName = parseTableName()

                load()
        }

        def load() {
                packageName = getPackageName()
                beanName = parseBeanName()
                columns = loadColumnNames()
                primaryKeys = loadPrimaryKeys()

//                deleteQuery = createDeleteQuery();
//                selectQuery = createSelectQuery();
//                updateQuery = createUpdateQuery();
//                insertQuery = createInsertQuery();
                src = createGroovyBean();
        }

        def parsePackageName() {
                //PropertiesFile p = new PropertiesFile("c:/projects/bumblezee/website/webdata/bumblezee/config.properties")
                def packageName = modelPackage

//                if (tableName.indexOf(".") == -1) {
//                        packageName = modelPackage
//                } else {
//                        packageName = tableName.substring(0, tableName.lastIndexOf("."))
//                }

		println packageName + modelPackage

		modelDir = System.getProperty("user.dir") + File.separator + "WEB-INF" + File.separator + packageName.replace('.', File.separator)

		println "modelDir: " + modelDir

                return packageName
        }

        def parseBeanName() {
                def b = '';
                def tbl = tableName
                if (tableName.indexOf('.') > -1) {
                        tbl = tableName.substring(tableName.lastIndexOf(".")+1, tableName.length());
                }

                if (tbl.toUpperCase().startsWith('TBL')) {
                        b = tbl.substring(3);
                } else {
                        b = tbl;
                }

                return b
        }

        def parseTableName() {
                if (tableName.indexOf('.') > -1) {
                        return tableName.substring(tableName.lastIndexOf(".")+1, tableName.length());
                } else {
                        return tableName
                }
        }


        def loadColumnNames() {
                def columns = [];
                //def meta = connection.getMetaData()
                //def rs = meta.getColumns(null,null,'TBLPROFILE','%')
                db.query('SELECT * FROM ' + tableName + ' WHERE 1=2') {rs ->
                        def meta = rs.metaData
                        def x = meta.columnCount

                        for (i in 0..<x) {
                                def col = new Column();
                                col.tableColumnName = meta.getColumnName(i + 1).toString().toLowerCase();
                                col.colType = meta.getColumnTypeName(i + 1);
                                col.javaType = getJavaType(col.colType, meta.getPrecision(i + 1));
                                col.propertyName = convertColumnNameToPropertyName(meta.getColumnName(i + 1));
                                col.primaryKey = isPrimaryKey(db.connection.getMetaData(), col.tableColumnName);
                                columns += col;
                                // println meta.getColumnName(i + 1) + ' - ' + meta.getPrecision(i+1)  + ' - ' +  meta.getColumnTypeName(i+1)
                        }
                }

                return columns
        }

        def loadPrimaryKeys() {
                def pk = [];
                for (col in columns) {
                        if (col.primaryKey) {
                                pk += col;
                        }
                }
                return pk;
        }

        def getJavaType(dbType, length) {
                if ((dbType == 'BIT' || dbType == 'TINYINT')&& length == 1)
                        return 'boolean'
                 System.out.println dbType
                switch (dbType) {

                        case ['VARCHAR', 'CHAR']:
                                return 'String'
                        case ['INTEGER','INT', 'SMALLINT', 'MEDIUMINT', 'TINYINT']:
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
                        case ['TIMESTAMP','DATETIME']:
                                return 'Timestamp'
                        case ['TEXT', 'LONGTEXT', 'ENUM', 'MEDIUMTEXT', 'CHAR']:
                                return 'Timestamp'
                        default:
                                if (dbType.toString().startsWith("BLOB"))
                                        return 'String'
                }
        }

        def convertColumnNameToPropertyName(colName) {
                colName = colName.toLowerCase();
                def prop = colName;

                while (true) {
                        def i = prop.indexOf("_");
                        if (i > -1) {
                                prop = prop.toString().substring(0, i) + prop.toString().substring(i + 1, i + 2).toUpperCase() + prop.toString().substring(i + 2);
                        } else {
                                break;
                        }
                }
                prop = prop.toString().substring(0, 1).toUpperCase() + prop.toString().substring(1);
                return prop;
        }


        def isPrimaryKey(meta, columnName) {
                def rset = meta.getPrimaryKeys(null, null, tableName);
                while (rset.next()) {
                        def idx = rset.getString('COLUMN_NAME').toLowerCase();

                        if (idx.equals(columnName)) {
                                return true;
                        }
                }
                return false;
        }

        def createDeleteQuery() {
                def sql;
                def val = '';
                def colNames = '';

                sql = 'DELETE FROM\n\t' + tableName + '\nWHERE\n';
                def whereClause = '';
                for (col in primaryKeys) {
                        whereClause += '\tAND ' + col.tableColumnName + '=' +
                                '#' + col.propertyName.substring(0, 1).toLowerCase() + col.propertyName.substring(1) + '#';
                }

                if (whereClause.trim().startsWith('AND')) {
                        whereClause = '\t' + whereClause.trim().substring(4);
                }

                return sql + whereClause;
        }

        def createSelectQuery() {
                def sql;
                def val = '';
                def colNames = '';

                for (col in columns) {
                        colNames += '\t' + col.tableColumnName + ' AS ' + col.propertyName.substring(0, 1).toLowerCase() + col.propertyName.substring(1) + ',\n ';
                }

                sql = 'SELECT\n' + colNames.substring(0, colNames.length() - 3) + '\nFROM \n\t' + tableName + '\nWHERE\n';
                def whereClause = '';
                for (col in primaryKeys) {
                        whereClause += '\tAND ' + col.tableColumnName + '=' +
                                '#' + col.propertyName.substring(0, 1).toLowerCase() + col.propertyName.substring(1) + '#';
                }

                if (whereClause.trim().startsWith('AND')) {
                        whereClause = '\t' + whereClause.trim().substring(4);
                }

                return sql + whereClause;
        }

        def createInsertQuery() {
                def sql;
                def val = '';
                def colNames = '';
                def whereClause = '';
                for (col in columns) {
                        colNames += '\t' + col.tableColumnName + ',\n ';
                        val += '\t#' + col.propertyName.substring(0, 1).toLowerCase() + col.propertyName.substring(1) + '#,\n ';

                }

                sql = "INSERT INTO " + tableName + " (\n" + colNames.substring(0, colNames.length() - 3) + "\n) VALUES (\n" + val.substring(0, val.length() - 3) + "\n)";

                return sql;
        }

        def createUpdateQuery() {
                def sql;
                def val = '';
                def colNames = '';

                for (col in columns) {
                        colNames += '\t' + col.tableColumnName + '=' +
                                '#' + col.propertyName.substring(0, 1).toLowerCase() + col.propertyName.substring(1) + '#,\n ';
                }

                sql = "UPDATE \n\t" + tableName + "\nSET\n" + colNames.substring(0, colNames.length() - 3) + "\nWHERE\n";
                def whereClause = '';
                for (col in primaryKeys) {
                        whereClause += '\tAND ' + col.tableColumnName + '=' +
                                '#' + col.propertyName.substring(0, 1).toLowerCase() + col.propertyName.substring(1) + '#';
                }

                if (whereClause.trim().startsWith('AND')) {
                        whereClause = '\t' + whereClause.trim().substring(4);
                }

                return sql + whereClause;
        }

        def createGroovyBean() {
                def src = '';

                src = "package $packageName\n\n"
                //src += 'import java.util.Date;\n\n';

                src += 'class ' + convertColumnNameToPropertyName(beanName) + ' {\n';

                src += '\tprivate def dynamicProperties = []\n'
                src += '\tstatic primaryKeys = ['
                boolean pks = false
                primaryKeys.each {
                        src += "'" + it.camelCaseName() + "', '"
                        pks = true
                }

                if (pks) {
                        src = src.substring(0, src.lastIndexOf(','))
                }
                src += ']\n\n'

                for (col in columns) {
                        src += '\t' + col.javaType + ' ' + lowerFirst(col.propertyName) + '\n';
                }

                src += '\n';

//                for (col in columns) {
//                        //colNames += '\t' + col.tableColumnName + '=' +
//                        //        '#' + col.propertyName.substring(0, 1).toLowerCase() + col.propertyName.substring(1) + '#,\n ';
//                        src += '\tpublic void set' + col.propertyName + ' (' + col.javaType + ' ' + col.propertyName + ') {\n';
//                        src += '\t\tthis.' + lowerFirst(col.propertyName) + ' = ' + col.propertyName + ';\n'
//                        src += '\t}\n\n';
//
//                        src += '\tpublic ' + col.javaType + ' get' + col.propertyName + ' () {\n';
//                        src += '\t\treturn ' + lowerFirst(col.propertyName) + ';\n'
//                        src += '\t}\n\n';
//                }

                src += '}';

                return src;
        }

        def createJavaBean() {
                def src = '';

                src = 'package model;\n\n';
                src += 'import java.util.Date;\n\n';

                src += 'public class ' + beanName + ' {\n';

                for (col in columns) {
                        src += '\tprivate ' + col.javaType + ' ' + lowerFirst(col.propertyName) + ';\n';
                }

                src += '\n';

                for (col in columns) {
                        //colNames += '\t' + col.tableColumnName + '=' +
                        //        '#' + col.propertyName.substring(0, 1).toLowerCase() + col.propertyName.substring(1) + '#,\n ';
                        src += '\tpublic void set' + col.propertyName + ' (' + col.javaType + ' ' + col.propertyName + ') {\n';
                        src += '\t\tthis.' + lowerFirst(col.propertyName) + ' = ' + col.propertyName + ';\n'
                        src += '\t}\n\n';

                        src += '\tpublic ' + col.javaType + ' get' + col.propertyName + ' () {\n';
                        src += '\t\treturn ' + lowerFirst(col.propertyName) + ';\n'
                        src += '\t}\n\n';
                }

                src += '}';

                return src;
        }

        def lowerFirst(s) {
                return s.substring(0, 1).toLowerCase() + s.substring(1);
        }

        def upperFirst(s) {
                return s.substring(0, 1).toUpperCase() + s.substring(1);
        }

        def writeGroovyBean() {
        	def dir = modelDir + File.separator
        	new File(dir).mkdirs()
                def camelCaseBeanName = convertColumnNameToPropertyName(beanName)
                def f = new FileOutputStream(dir + camelCaseBeanName + '.groovy')
                f.withWriter {w ->
                        w << src;
                }
                System.out.println  'bean created: ' + dir + camelCaseBeanName + '.groovy'
        }

        def writeXML() {
                def f = new FileOutputStream(beanName + '.java')
                f.withWriter {w ->
                        w << src;
                }

                f = new FileOutputStream(beanName + '.xml')
                f.withWriter {w ->
                        w << '<?xml version="1.0" encoding="UTF-8" ?>\n'
                        w << '\t<queries>\n'

                        getQueryTags(selectQuery, 'select.' + beanName, w);
                        getQueryTags(insertQuery, 'insert.' + beanName, w);
                        getQueryTags(updateQuery, 'update.' + beanName, w);
                        getQueryTags(deleteQuery, 'delete.' + beanName, w);
                        w << '\t</queries>'
                }
        }

        def getQueryTags(sql, id, out) {
                if (id.startsWith('select'))
                        out << '\t\t<query id="' + id + '" resultClass=\"model.' + beanName + '\">\n'
                else
                        out << '\t\t<query id="' + id + '">\n'

                out << '\t\t<![CDATA[\n'

                sql.split('\n').each {line ->
                        out << '\t\t\t' + line + '\n'
                }

                out << '\t\t]]>\n'
                out << '\t\t</query>\n\n'
        }
}



class Column {
        String colType;
        String tableColumnName;
        String propertyName;
        String javaType;
        boolean primaryKey

        def String camelCaseName() {
                propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1)
        }
}


public class PropertiesFile {
        private static final Logger log = Logger.getLogger(PropertiesFile.class.getName());
        private Properties prop;
        private String fileName;

        public PropertiesFile(String fileName) {
                this.fileName = fileName;
                load();
        }

        public void load() {
                prop = new Properties();
                FileInputStream fis = null;
                try {
                        if (fileName.toLowerCase().startsWith("classpath:")) {
                                URL url = getClass().getClassLoader().getResource(fileName.substring(10));
                                fis = new FileInputStream(url.getFile());
                                prop.load(fis);
                        } else {
                                fis = new FileInputStream(new File(fileName));
                                prop.load(fis);
                        }

                } catch (Exception e) {
                        log.logMessage(Level.FINEST, "PropertiesFile.load() failed. message:" + e.getMessage(), e);
                } finally {
                        try {
                                fis.close();
                        } catch (IOException e) {
                        }
                }
        }

        public String getString(String key) {
                return prop.get(key).toString();
        }

        public Integer getInt(String key) {
                try {
                        return Integer.parseInt(getString(key));
                } catch (Exception e) {
                        log.logMessage(Level.FINEST, "PropertiesFile.getInt() failed for key: " + key, e);
                }

                return null;
        }

}
