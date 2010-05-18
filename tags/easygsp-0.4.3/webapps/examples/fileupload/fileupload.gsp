<%
              
        import groovy.sql.Sql  
        import org.apache.commons.fileupload.FileItem

        def uploadLocation=''

        request.uploads.each {
                FileItem item = it.value

                File uploadedFile = new File(application.appPath + File.separator + item.name);
                //uploadedFile.mkdirs();
                item.write(uploadedFile);
                uploadLocation = 'File uploaded to: ' + uploadedFile.canonicalPath

                println("file upload" + item);
        }


%>


<html>
        <head>
                <title>File Upload Example</title>
        </head>
        <body>
                ${uploadLocation}
                <form action="fileupload.gsp" method="POST" enctype="multipart/form-data">
                        File: <input type="file" name="file"/><br/>
                        <input type="submit" name="submit" value="Upload File"/>
                </form>

        </body>
</html>

