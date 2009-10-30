import util.*
import model.*

class web {
        def onApplicationStart(app) {
                Properties prop = new Properties()
                prop.load(new FileInputStream(app.appPath + File.separator + "WEB-INF" + File.separator + "db.properties"))
                app.setAttribute("db.properties", prop)

                SimpleDaoUtil.databaseUrl = prop.get("database.url")
                SimpleDaoUtil.databaseUsername = prop.get("database.username")
                SimpleDaoUtil.databasePassword = prop.get("database.password")
                SimpleDaoUtil.databaseDriver = prop.get("database.driver")
                                                                   
                SimpleDaoUtil.populate(model.Game.class)
                SimpleDaoUtil.populate(model.GameTitle.class)
                SimpleDaoUtil.populate(model.GameGenre.class)
                SimpleDaoUtil.populate(model.GameSystem.class)
        }                                 

        def onApplicationEnd(app) {
        }

        def onSessionStart(session) {

        }

        def onSessionEnd(Object session) {

        }
}