
import groovy.sql.Sql

def sql = Sql.newInstance("jdbc:firebirdsql:localhost/3050:c:/projects/scgi/databases/app1/app1.fdb", "sysdba",
"masterkey", "org.firebirdsql.jdbc.FBDriver")

// defs allow watches in intellij
def meta = sql.connection.getMetaData()
def _types = ["TABLE"]
def types = (String[]) _types
def rs = meta.getTables(null, null, '%', types);

table = []

while (rs.next()) {
       table << rs.getString("TABLE_NAME");
}



//        table.each{
//               System.out.println(it)
//        }

request.setAttribute("tables", table)
forward("templates/tables.gsp")

sql.connection.close()
