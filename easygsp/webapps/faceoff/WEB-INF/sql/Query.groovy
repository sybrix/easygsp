package sql

import groovy.sql.Sql
import util.DAOUtil

class Query {
        def Sql dbHandle

        def getDb() {
                if (dbHandle == null) {
                        def source = Sql.newInstance(
                                DAOUtil.databaseUrl,
                                DAOUtil.databaseUsername,
                                DAOUtil.databasePassword,
                                DAOUtil.databaseDriver
                        )


                        dbHandle = new Sql(source)
                }
                
                return dbHandle
        }

        def findComments(Integer frameworkId) {
                db.rows("""
                SELECT
                        c.comment_id commentId,
                        c.comment,
                        c.framework_id frameworkId,
                        c.profile_id profileId,
                        c.created,
                        (case c.profile_id
                        	when 1 then c.email
                        	else p.email
                        end) as email,
                        c.screen_name screenName
                FROM
                        faceoff_framework_comment c
                        LEFT JOIN faceoff_profile p
                               ON c.profile_id = p.profile_id
                WHERE
                        c.framework_id = $frameworkId
                ORDER BY
                        c.created DESC

                """)
        }


        def listFrameworks() {
                db.rows("""
                SELECT
                        framework_id frameworkId,
                        framework,
                        version,
                        url,
                        description,
                        score,
                        c.category
                FROM
                        faceoff_framework f
                        INNER JOIN faceoff_category c
                               ON f.category_id = c.category_id
                ORDER BY
                        c.category, framework

                """)
        }
}