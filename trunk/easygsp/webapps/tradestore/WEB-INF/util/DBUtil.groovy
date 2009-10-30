package util;
import groovy.sql.Sql

class DBUtil {
	
        def static getDb(){    
	        	Sql.newInstance(SimpleDaoUtil.databaseUrl, SimpleDaoUtil.databaseUsername, SimpleDaoUtil.databasePassword, SimpleDaoUtil.databaseDriver)
        }
}