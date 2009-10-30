

import groovy.sql.Sql


request.setAttribute("title", "view template")

def sql = Sql.newInstance("jdbc:firebirdsql:localhost/3050:c:/projects/scgi/databases/app1/app1.fdb", "sysdba",
        "masterkey", "org.firebirdsql.jdbc.FBDriver")

sql.eachRow("select * from tblProfile"){row->
        System.out.println row.first_name
}

//System.out.println("hello")
//File f = new File("c:/projects/scgi/webapps/app2/index.groovy")
//f.isDirectory()
// System.out.println  f.name

//request.getRequestDispatcher("show.groovy").forward(request, response)

request.templateForward("../view.gsp")