import org.firebirdsql.management.FBManager
import org.firebirdsql.jdbc.FBDriver

import java.sql.DriverManager
import java.sql.Connection
import java.sql.Statement

def webInfDir = System.getProperty("user.dir") + File.separator + args[2] + File.separator + "WEB-INF" + File.separator
def webGroovy = webInfDir + "web.groovy"
def routesGroovy = webInfDir + "routes.groovy"
def appPath = System.getProperty("user.dir") + File.separator + args[2] + File.separator


def propFilePath = webInfDir + "db.properties"

// create standard directories
new File(webInfDir + "db").mkdirs()
new File(webInfDir + "models").mkdirs()
new File(appPath + "media" + File.separator + "css").mkdirs()
new File(appPath + "media" + File.separator + "images").mkdirs()
new File(appPath + "media" + File.separator + "js").mkdirs()


// CREATE DATABASE
def dbPath = webInfDir + "db" + File.separator + args[2] + ".fdb"
String dbUser = "sysdba";
String dbPassword = "masterkey";
String dbUrl = "jdbc:firebirdsql:localhost/3050:${dbPath}"
new File(propFilePath).write("""
database.driver=org.firebirdsql.jdbc.FBDriver
database.url=${dbUrl}
database.password=${dbUser}
database.username=${dbPassword}"""
)

Class.forName("org.firebirdsql.jdbc.FBDriver")

FBManager fbManager = new FBManager();
fbManager.setServer("localhost");
fbManager.setPort(3050);
fbManager.start();
fbManager.createDatabase(dbPath, dbUser, dbPassword);
fbManager.stop()

Connection bd = DriverManager.getConnection(dbUrl,dbUser,dbPassword)
Statement st = bd.createStatement()
st.execute("UPDATE rdb\$database SET rdb\$character_set_name='UTF8'")
bd.close()

// CREATE PROPERTIES FILES
def appPropFilePath = webInfDir + "app.properties"
new File(appPropFilePath).write("""
#enter application properties
#app.property"""
)

new File(webGroovy).write("""
class web {

        def onApplicationStart(app) {
		loadAllProperties(app)
        }

        def onApplicationEnd(app) {
        }

        def onSessionStart(session) {
        }

        def onSessionEnd(session) {
        }

        def onChanged(app, path){
        }

        def loadAllProperties(app){
                ['db.properties','app.properties'].each {
                        def propFile = loadPropertiesFile(it)
                        def en = propFile.propertyNames()
                        while(en.hasMoreElements()){
                                String key = en.nextElement()
                                app[key.trim()] = propFile.get(key).trim()
                        }
                }
	}

}""")


new File(routesGroovy).write("""
// Format: add 'method', 'path', 'gsp_page'
// i.e. add 'GET', '/product/{id}', '/products/item.gspx'
// The dynamic parameter are available as request parameters in the controller and gsp page

""")


// CREATE INDEX HOME PAGE
new File(appPath + "index.groovy").write("""
processPage(this, [''])

def load(){
	['today': new Date()]
}
""")

new File(appPath + "index.gspx").write("""
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"  itemscope itemtype="http://schema.org/Product">
<head>
	<title>App Title</title>
    	<link href="http://cdn.kendostatic.com/2011.3.1407/styles/kendo.common.min.css" rel="stylesheet" />
    	<link href="http://cdn.kendostatic.com/2011.3.1407/styles/kendo.default.min.css" rel="stylesheet" />
    	<script src="http://cdn.kendostatic.com/2011.3.1407/js/kendo.all.min.js"></script>
    	<script src="http://code.jquery.com/jquery-1.7.1.min.js"></script>
	<link href="media/css/style.css" rel="stylesheet" type="text/css" />
</head>
<body>
Today is \${today}
</body>
</html>
""")


new File(appPath + "media/css/style.css").write("""
body {
	font-family: Verdana, Geneva, sans-serif;
	font-size: 1.2em;
}
""")





