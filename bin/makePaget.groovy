import org.firebirdsql.management.FBManager
import org.firebirdsql.jdbc.FBDriver

import java.sql.DriverManager
import java.sql.Connection
import java.sql.Statement

def gspPage = System.getProperty("user.dir") + File.separator + args[2] + ".gspx"
def groovyPage = System.getProperty("user.dir") + File.separator + args[2] + ".groovy"


new File(groovyPage).write("""
import services.*
import model.*

processPage(this, [])

def load(){
      
}
""")


new File(gspPage).write("""
<%@ extends 'mainTemplate.gspx' %>

<%@ block main %>
        

<%@ endblock %>

""")


