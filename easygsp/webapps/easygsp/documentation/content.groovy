import groovy.sql.Sql
import util.*

WebUtil.process(this, [])


def get() {
        findDocumentation(params.name_id)
}


def findDocumentation(nameId) {
        if (nameId == null) {
                bind 'title', ''
                bind 'documentation', ''
                bind 'nameId', ''
                return
        }

        db.eachRow("SELECT title, documentation, name_id nameId from easy_documentation where name_id = $nameId") {

                bind 'title', it.title
                bind 'documentation', it.documentation
                bind 'nameId', it.nameId


        }
}

def getDb() {
        def sql = Sql.newInstance("jdbc:mysql://localhost:3306/sybrixApps", "root", "root", "com.mysql.jdbc.Driver")
}
