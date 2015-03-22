EasyGSP adds a method to all classes, controllers and templates for obtaining a groovy.sql.Sql object.

#### public groovy.sql.Sql newSqlInstance(String dataSource) ####

In order to use this method, the database attributes must be set as application attributes.
The best place to do this is in the [onApplicationStart(app)](firstproject.md) event of the WEB-INF/web.groovy file.

The following attributes must be set:
| database.driver |
|:----------------|
| database.url |
| database.password |
| database.username |

The best place to store these would be in a properties.  Here's a code sample that reads the attributes from a properties file and adds them to the application object when onApplicationStart event is fired.

### The Set Up ###
web.groovy
```

class web {
	def onApplicationStart(app){                
       // if this was real, yes, I would loop thru the properties
                def propFile = loadPropertiesFile('db.properties')
                app['database.url'] = propFile.get('database.url')
                app['database.password'] = propFile.get('database.password')          
                app['database.username'] = propFile.get('database.username')
                app['database.driver'] = propFile.get('database.driver')

                // setup easygspDB datasource
                app['easygspDB.database.url'] = propFile.get('easygspDB.database.url')
                app['easygspDB.database.password'] = propFile.get('easygspDB.database.password')          
                app['easygspDB.database.username'] = propFile.get('easygspDB.database.username')
                app['easygspDB.database.driver'] = propFile.get('easygspDB.database.driver')

                 
        }
```


The properties file: db.properties
```
database.driver=com.mysql.jdbc.Driver
database.url=jdbc:mysql://localhost:3306/mysql
database.password=superSecretPassword
database.username=root

# name prefix(easygspDB) will serve as "datasource" name
easygspDB.database.database.driver=com.mysql.jdbc.Driver
easygspDB.database.url=jdbc:mysql://localhost:3306/easygspDB
easygspDB.database.password=superSecretPassword
easygspDB.database.username=root
```

### And now newSqlInstance() ###
index.gsp
```
    def db = newSqlInstance()

    d.rows("SELECT host FROM user").each {
        println it.host
   }
```


To connect to the other database in the properties file, specify the datasource name:
```
    def db = newSqlInstance('easygspDB')

    d.rows("SELECT user FROM user").each {
        println it.user
   }
```