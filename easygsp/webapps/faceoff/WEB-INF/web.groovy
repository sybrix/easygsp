import util.*
            
class web {     
        def onApplicationStart(app) {
                              
                Properties prop = new Properties()
                prop.load(new FileInputStream(app.appPath + File.separator + "WEB-INF" + File.separator + "db.properties"))
                System.out.println(app.appPath + File.separator + "WEB-INF" + File.separator + "db.properties")
                app.setAttribute("db.properties", prop)

                DAOUtil.databaseUrl = prop.get("database.url")
                DAOUtil.databaseUsername = prop.get("database.username")
                DAOUtil.databasePassword = prop.get("database.password")
                DAOUtil.databaseDriver = prop.get("database.driver")

                DAOUtil.populate(model.FaceoffProfile.class)   
                DAOUtil.populate(model.FaceoffFrameworkComment.class)
                DAOUtil.populate(model.FaceoffFramework.class)
                DAOUtil.populate(model.FaceoffCategory.class)
                DAOUtil.populate(model.FaceoffScoreItem.class)
                
        }         

        def onApplicationEnd(Object app) {
                System.out.println("ending application")
        }

        def onSessionStart(Object session) {
                session.setAttribute('profileId',1)
                System.out.println("starting session :" + session.servletContext.appName)
        }

        def onSessionEnd(Object session) {
                System.out.println("ending session")
        }
}