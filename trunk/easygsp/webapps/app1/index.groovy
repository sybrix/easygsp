import groovy.sql.Sql  
import org.apache.commons.fileupload.FileItem
import model.*

 new myclass().sayHello() 

request.setAttribute("title", "view template")

request.getSession(true)
session.setAttribute("myname", "David SmithXXX");

Properties prop = new Properties()
prop.load(new FileInputStream(application.appPath + File.separator + "WEB-INF" + File.separator + "db.properties"))
//
System.out.println(prop.get("database.url"))

Profile p = new Profile(lastName:'david smith')                 
p.insert()                                                                   
                                                     
def sql = Sql.newInstance("jdbc:firebirdsql:localhost/3050:c:/projects/easygsp/easygsp/databases/app1/app1.fdb", "sysdba","masterkey", "org.firebirdsql.jdbc.FBDriver")
        // defs allow watches in intellij
        def meta = sql.connection.getMetaData()
        def _types = ["TABLE"]
        def types = (String[]) _types
        def rs = meta.getTables(null, null, '%', types);

        table = []
        i = 0;
        while (rs.next()) {
                System.out.println(rs.getString("TABLE_NAME"))
                table[i++] = rs.getString("TABLE_NAME");
        }

                    
//log 'log message from index.groovy', new Exception()
//sql.eachRow("select * from tblProfile"){row->
//        System.out.println row.first_name
//}

try {
         throw new Exception("ok");
} catch(e){
        log  e
        log 'failed', e
}
log 'log message from index.groovy'

System.out.println("hello")
File f = new File("c:/projects/easygsp/easygsp/webapps/app1/index.groovy")
f.isDirectory()

FileReader r = new FileReader(f)
String xx = r.readLine()
System.out.println  xx
//
//
//
//System.out.println  session.id

//request.getRequestDispatcher("show.groovy").forward(request, response)

//println """
//        <html>
//                <head>
//                        <title>hey man</title>
//                <head>
//        </html>
//"""

//System.exit(0)

//new javax.swing.JFrame("my Window").setVisible(true);
//                Thread.start("my thread"){
//                try {
//                       // synchronized (this){
//                                System.out.println("started...")
//                                Thread.sleep(15000)
////                        }
//                }catch(Exception  e){
//                        e.printStackTrace()
//
//                } finally{
//                       System.out.println("done")
//                }
//}



//
//                for(int x=0;x<10;x++){
//                        System.out.println("looping x" + x)
//                        sleep(1000);
//                }

        for(FileItem item:request.parseFileUploads()){
                if (!item.isFormField()){
                        File uploadedFile = new File("c:\\projects\\easygsp\\easygsp\\webapps\\app1\\file.pdf");
                        item.write(uploadedFile);
                }
                System.out.println("file upload" + item);
        }



request.setAttribute("date" , new java.util.Date())
forward("view.gspx")
