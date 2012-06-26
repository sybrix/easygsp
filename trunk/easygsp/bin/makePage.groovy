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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>MyNewPage</title> 
		<link href="/media/css/style.css" rel="stylesheet" type="text/css" 
		<script type="text/javascript" src="/media/js/jquery-1.7.min.js"></script>
		
		<%@ script %>
			<script type="text/javascript">
			
			        jQuery(document).ready(function(\$) {
			
			        
			        })
			      
			</script>
		<%@ endScript> 
		<style>
		
		</style>
	</head>
	<body>
	
	
	</body>
</html>

""")


