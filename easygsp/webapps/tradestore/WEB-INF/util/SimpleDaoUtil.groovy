package util

import org.codehaus.groovy.runtime.metaclass.ThreadManagedMetaBeanProperty
import groovy.sql.Sql
import org.codehaus.groovy.runtime.GStringImpl

import java.util.logging.Logger
import javax.sql.DataSource
import javax.naming.NamingException


class SimpleDaoUtil {
        private static final Logger log = Logger.getLogger(SimpleDaoUtil.class.getName())

        public static boolean useTablePrefix=true;
        public static String tablePrefix=""
        public static boolean camelCaseTableNames=true
        public static boolean camelCaseColumnNames=true

        public static String databaseUrl
        public static String databaseUsername
        public static String databasePassword
        public static String databaseDriver

        public static void populate(Class clazz) {
                addDAOProperties(clazz)
                overrideSetProperty(clazz)
                addClearMethod(clazz)
                addSaveMethod(clazz)
                addInsertMethod(clazz)
                addUpdateMethod(clazz)
                addDeleteMethod(clazz)
                addStaticDeleteMethod(clazz)
                addFindMethod(clazz)
                addFindAllMethod(clazz)
                addListMethod(clazz)
        }

        public static void addDAOProperties(clazz) {
                clazz.metaClass.dataSource = ''
                clazz.metaClass.tableName = getTableName(clazz)
        }

        public static void overrideSetProperty(clazz) {
                clazz.metaClass.setProperty = {String name, value ->
                        def metaProperty = clazz.metaClass.getMetaProperty(name)
                        if (metaProperty instanceof ThreadManagedMetaBeanProperty) {
                                metaProperty.setThreadBoundPropertyValue(delegate, name, value)
                        } else {
                                metaProperty.setProperty(delegate, value)
                                delegate.dynamicProperties << name
                                log.fine "setProperty $name"
                        }
                }
        }

        public static void addInsertMethod(clazz) {
                clazz.metaClass.insert = {->

                        def columnName
                        def values = []
                        StringBuilder s = new StringBuilder()

                        s.append "INSERT INTO $tableName ("
                        boolean hasColumnsProperty = isProperty(clazz, 'columns')

                        delegate.dynamicProperties.each {
                                if (hasColumnsProperty && delegate.columns?.containsKey(it)) {
                                        s << delegate.columns[it]
                                } else {
                                        s << unCamelCase(it)
                                }

                                s << ', '
                        }

                        s.replace(s.size() - 2, s.size(), '')

                        s << ') VALUES ('

                        delegate.dynamicProperties.each {
                                s << '?,'

                                if (delegate."$it" instanceof java.util.Date) {
                                        values << new java.sql.Date(delegate."$it".time)
                                } else if (delegate."$it" instanceof java.lang.Boolean) {
                                        values << delegate."$it" == true ? '1'.toCharacter() : '0'.toCharArray()
                                } else {
                                        values << delegate."$it"
                                }
                        }
                        s.replace(s.size() - 1, s.size(), '')
                        s << ')'

                        log.fine s.toString()

                        def db = Sql.newInstance(getSqlInstance(delegate.dataSource))
                        db.execute(new GStringImpl(values.toArray(), s.toString().trim().split('\\?')))
                }
        }

        private static def getValue(obj, val) {
                if (obj == java.sql.Date.class) {
                        return new java.util.Date(val.time)
//                } else if (obj == java.lang.Boolean.class || obj == boolean.class) {
//                        return val == '1'
                } else {
                        return val
                }

        }
        public static void addSaveMethod(clazz) {
                clazz.metaClass.save = {->
                        boolean hasPrimaryKeyValues = false;
                        clazz.primaryKeys.each {
                                def val =  delegate?."$it"
                                if (val != null)
                                        hasPrimaryKeyValues = true;
                                System.out.println "primaryKey value:" +  val
                        }

                        if (hasPrimaryKeyValues)
                                update();
                        else
                                insert();
                }
        }

        public static void addUpdateMethod(clazz) {
                clazz.metaClass.update = {->


                        def values = []
                        def sql = new StringBuilder()
                        def columnName

                        sql << "UPDATE $tableName SET "

                        delegate.dynamicProperties.each {
                                columnName = unCamelCaseColumn(it)
                                sql << "$columnName = ?, "
                                values << delegate?."$it"
                        }
                        sql.replace(sql.size() - 2, sql.size(), '')

                        sql << " WHERE "

                        clazz.primaryKeys.each {
                                columnName = unCamelCaseColumn(it)
                                sql << "$columnName = ?, and "
                                values << delegate?."$it"
                        }

                        sql.replace(sql.size() - 6, sql.size(), '')

                        log.fine sql.toString()

                        def db = Sql.newInstance(getSqlInstance(delegate?.dataSource))
                        db.executeUpdate(new GStringImpl(values.toArray(), sql.toString().trim().split('\\?')))
                }
        }


        public static void addFindMethod(clazz) {

                clazz.metaClass.'static'.find = {whereMap ->
                        def values = []
                        def sql = new StringBuilder()
                        String columnName
                        def tbl = getTableName(clazz)

                        sql << "SELECT "
                        Map columns = [:]
                        boolean hasColumnsProperty = isProperty(clazz, 'columns')

                        clazz.declaredFields.each {
                                def prop = clazz.metaClass.getMetaProperty(it.name)
                                if (clazz.metaClass.getMetaProperty(it.name) && !it.name.equals('metaClass') && (prop instanceof MetaBeanProperty)) {
                                        if (!prop.field.isStatic()) {
                                                if (hasColumnsProperty && delegate.columns?.containsKey(it.name)) {
                                                        sql << delegate.columns[it.name] << ' as ' << it.name
                                                        columns[it.name] = delegate.columns[it.name]
                                                } else {
                                                        sql << unCamelCaseColumn(it.name) << ' as ' << it.name
                                                        columns[it.name] = unCamelCaseColumn(it.name)
                                                }
                                                sql << ', '

                                        }
                                }
                        }

                        sql.replace(sql.size() - 2, sql.size(), ' ')

                        sql << "FROM $tbl WHERE "

                        whereMap.each {
                                if (hasColumnsProperty && delegate.columns?.containsKey(it.key)) {
                                        columnName = delegate."$columns[it.key]"
                                } else {
                                        columnName = unCamelCaseColumn(it.key)
                                }

                                sql << "$columnName = ? and "
                                values << it.value
                        }

                        sql.replace(sql.size() - 4, sql.size(), '')

                        log.fine sql.toString()

                        def db = Sql.newInstance(getSqlInstance(null))
                        List results = new ArrayList()

                        db.eachRow(new GStringImpl(values.toArray(), sql.toString().trim().split('\\?'))) {rs ->
                                def row = clazz.newInstance();
                                columns.each {col ->
                                        row."$col.key" = getValue(getType(clazz, "$col.key"), rs."$col.key")
                                }
                                row.clearDynamicProperties()
                                results << row
                        }

                        results.get(0)
                }

        }


        public static void addFindAllMethod(clazz) {
                clazz.metaClass.'static'.findAll = {whereMap ->
                        def values = []
                        def sql = new StringBuilder()
                        String columnName
                        def tbl = getTableName(clazz)

                        def orderBy = whereMap.remove("orderBy")

                        sql << "SELECT "
                        Map columns = [:]
                        boolean hasColumnsProperty = isProperty(clazz, 'columns')
                        clazz.declaredFields.each {
                                def prop = clazz.metaClass.getMetaProperty(it.name)
                                if (clazz.metaClass.getMetaProperty(it.name) && !it.name.equals('metaClass') && (prop instanceof MetaBeanProperty)) {
                                        if (!prop.field.isStatic()) {
                                                if (hasColumnsProperty && delegate.columns?.containsKey(it.name)) {
                                                        sql << delegate.columns[it.name] << ' as ' << it.name
                                                        columns[it.name] = delegate.columns[it.name]
                                                } else {
                                                        sql << unCamelCaseColumn(it.name) << ' as ' << it.name
                                                        columns[it.name] = unCamelCaseColumn(it.name)
                                                }
                                                sql << ', '
                                        }
                                }
                        }

                        sql.replace(sql.size() - 2, sql.size(), ' ')

                        sql << "FROM $tbl WHERE "
                        whereMap.each {
                                if (hasColumnsProperty && delegate.columns?.containsKey(it.key)) {
                                        columnName = delegate."$columns[it.key]"
                                } else {
                                        columnName = unCamelCaseColumn(it.key)
                                }

                                sql << "$columnName = ? and "
                                values << it.value
                        }

                        sql.replace(sql.size() - 4, sql.size(), '')

                        parseOrderBy(sql, orderBy, clazz, columns)


                        log.fine sql.toString()

                        def db = Sql.newInstance(getSqlInstance(null))
                        List results = new ArrayList()

                        db.eachRow(new GStringImpl(values.toArray(), sql.toString().trim().split('\\?'))) {rs ->
                                def row = clazz.newInstance();
                                columns.each {col ->
                                        row."$col.key" = getValue(getType(clazz, "$col.key"), rs."$col.key")
                                }
                                row.clearDynamicProperties()
                                results << row
                        }

                        return results
                }
        }

        public static void addListMethod(clazz) {
                clazz.metaClass.'static'.list = {optionsMap ->
                        def values = []
                        def sql = new StringBuilder()
                        String columnName
                        def tbl = getTableName(clazz)
                        def orderBy

                        if (optionsMap instanceof Map)
                                orderBy = optionsMap.remove("orderBy")

                        sql << "SELECT "
                        Map columns = [:]
                        boolean hasColumnsProperty = isProperty(clazz, 'columns')
                        clazz.declaredFields.each {
                                def prop = clazz.metaClass.getMetaProperty(it.name)
                                if (clazz.metaClass.getMetaProperty(it.name) && !it.name.equals('metaClass') && (prop instanceof MetaBeanProperty)) {
                                        if (!prop.field.isStatic()) {
                                                if (hasColumnsProperty && delegate.columns?.containsKey(it.name)) {
                                                        sql << delegate.columns[it.name] << ' as ' << it.name
                                                        columns[it.name] = delegate.columns[it.name]
                                                } else {
                                                        sql << unCamelCaseColumn(it.name) << ' as ' << it.name
                                                        columns[it.name] = unCamelCaseColumn(it.name)
                                                }
                                                sql << ', '
                                        }
                                }
                        }

                        sql.replace(sql.size() - 2, sql.size(), ' ')

                        sql << "FROM $tbl "

                        parseOrderBy(sql, orderBy, clazz, columns)

                        log.fine sql.toString()

                        def db = Sql.newInstance(getSqlInstance(null))
                        List results = new ArrayList()

                        db.eachRow(new GStringImpl(values.toArray(), sql.toString().trim().split('\\?'))) {rs ->
                                def row = clazz.newInstance();
                                columns.each {col ->
                                        row."$col.key" = getValue(getType(clazz, "$col.key"), rs."$col.key")
                                }
                                row.clearDynamicProperties()
                                results << row
                        }

                        return results
                }
        }

        public static void addClearMethod(clazz) {
                clazz.metaClass.clearDynamicProperties = {->
                        delegate?.dynamicProperties.clear()
                }
        }

        public static void addDeleteMethod(clazz) {
                clazz.metaClass.delete = {->
                        def values = []
                        def sql = new StringBuilder()
                        def columnName

                        sql << "DELETE FROM $tableName WHERE "

                        clazz.primaryKeys.each {
                                columnName = unCamelCaseColumn(it)
                                sql << "$columnName = ?, and "
                                values << delegate?."$it"
                        }

                        sql.replace(sql.size() - 6, sql.size(), '')

                        log.fine sql.toString()

                        def db = Sql.newInstance(getSqlInstance(delegate?.dataSource))
                        db.executeUpdate(new GStringImpl(values.toArray(), sql.toString().trim().split('\\?')))
                }
        }

        public static void addStaticDeleteMethod(clazz) {
                clazz.metaClass.'static'.delete = {Map whereMap ->
                        def values = []
                        def sql = new StringBuilder()
                        def columnName
                        def tbl = getTableName(clazz)

                        sql << "DELETE FROM $tbl WHERE "
                        boolean hasColumnsProperty = isProperty(clazz, 'columns')
                        whereMap.each {
                                if (hasColumnsProperty && delegate.columns?.containsKey(it.key)) {
                                        columnName = delegate."$columns[it.key]"
                                } else {
                                        columnName = unCamelCaseColumn(it.key)
                                }

                                sql << "$columnName = ? and "
                                values << it.value
                        }

                        sql.replace(sql.size() - 6, sql.size(), '')

                        log.fine sql.toString()

                        def db = Sql.newInstance(getSqlInstance(null))
                        db.executeUpdate(new GStringImpl(values.toArray(), sql.toString().trim().split('\\?')))
                }
        }

        private static def getTableName(Class clazz) {
                String tableName

                if (propertyExist(clazz, 'tableName')) {
                        def metaProperty = clazz.metaClass.getMetaProperty('tableName')
                        if (metaProperty instanceof ThreadManagedMetaBeanProperty)
                                return metaProperty.initialValue else
                                return metaProperty.getProperty('tableName')
                } else {
                        if (clazz.name.lastIndexOf('.') > -1) {
                                tableName = clazz.name.substring(clazz.name.lastIndexOf('.') + 1)
                        } else {
                                tableName = clazz.name
                        }
                }

                if (camelCaseTableNames == true) {
                        tableName = unCamelCase(tableName)
                }

                if (useTablePrefix == true) {
                        return tablePrefix + tableName
                } else {
                        return tableName
                }
        }



        private static String camelCase(String column) {
                StringBuffer newColumn = new StringBuffer();
                boolean underScoreFound = false;
                int index = -1;
                int currentPosition = 0;
                while ((index = column.indexOf('_', currentPosition)) > -1) {
                        newColumn.append(column.substring(currentPosition, index).toLowerCase());
                        newColumn.append(column.substring(index + 1, index + 2).toUpperCase());

                        currentPosition = index + 2;
                        underScoreFound = true;
                }

                if (underScoreFound == false) {
                        return column;
                } else {
                        newColumn.append(column.substring(currentPosition, column.length()).toLowerCase());
                }

                return newColumn.toString();
        }

        private static String unCamelCase(String column) {
                StringBuffer newColumn = new StringBuffer();
                for (int i = 0; i < column.length(); i++) {
                        if (Character.isLetter(column.charAt(i)) && Character.isUpperCase(column.charAt(i))) {
                                if (i > 0)
                                        newColumn.append("_");

                                newColumn.append(Character.toLowerCase(column.charAt(i)));
                        } else {
                                newColumn.append(column.charAt(i));
                        }
                }

                return newColumn.toString();
        }


        static Sql getSqlInstance(String dataSource) {
                        return Sql.newInstance(databaseUrl, databaseUsername, databasePassword, databaseDriver)
        }

        def static camelCaseColumn(String column) {
                if (camelCaseColumnNames == true) {
                        return camelCase(column)
                } else {
                        return column
                }
        }

        def static unCamelCaseColumn(String column) {
                if (camelCaseColumnNames == true) {
                        return unCamelCase(column)
                } else {
                        return column
                }
        }

        static boolean isDynamicProperty(Class clazz, String propertyName) {
                def metaProperty = clazz.metaClass.getMetaProperty(propertyName)
                if (metaProperty instanceof ThreadManagedMetaBeanProperty) {
                        return true
                } else {
                        return false
                }
        }

        static boolean isProperty(Class clazz, String propertyName) {
                def metaProperty = clazz.metaClass.getMetaProperty(propertyName)
                if (metaProperty instanceof MetaBeanProperty) {
                        return true
                } else {
                        return false
                }
        }

        static boolean propertyExist(Class clazz, String propertyName) {
                MetaBeanProperty metaProperty = clazz.metaClass.getMetaProperty(propertyName)
                if (metaProperty != null) {
                        return true
                } else {
                        return false
                }
        }

        static Class getType(Class clazz, String propertyName) {
                MetaBeanProperty metaProperty = clazz.metaClass.getMetaProperty(propertyName)
                return metaProperty.getSetter().getNativeParameterTypes()[0]
        }

        def static getColumnName(Class clazz, String propertyName, columns) {
                boolean hasColumnsProperty = isProperty(clazz, 'columns')

                def prop = clazz.metaClass.getMetaProperty(propertyName)
                //def columnsMap = clazz?.columns;
                def columnsMap = columns;
                //System.out.println(columnsMap)

                if (clazz.metaClass.getMetaProperty(propertyName) && (prop instanceof MetaBeanProperty)) {
                        if (!prop.field.isStatic()) {
                                if (hasColumnsProperty && columnsMap?.containsKey(propertyName)) {
                                        return columnsMap[propertyName]
                                } else {
                                        return unCamelCaseColumn(propertyName)
                                }
                        }
                }
        }

        private static def parseOrderBy(sql, orderBy, clazz, columns) {
                def orderByAry
                if (orderBy != null) {
                        sql << "ORDER BY "

                        orderByAry = orderBy.split(',')

                        orderByAry.each {
                                def orderByPart = it.trim().split(' ')

                                sql << getColumnName(clazz, orderByPart[0], columns)
                                if (orderByPart.size() > 1)
                                        sql << ' ' << orderByPart[1]
                                sql << ','
                        }

                        sql.setLength(sql.length() - 1)
                }
        }
}