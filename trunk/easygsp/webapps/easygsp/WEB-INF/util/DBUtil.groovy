package util;
import groovy.sql.Sql

class DBUtil {
        def static getDb(){
                 Sql.newInstance("jdbc:mysql://localhost:3306/sybrixApps", "root","root", "com.mysql.jdbc.Driver")
        }
}