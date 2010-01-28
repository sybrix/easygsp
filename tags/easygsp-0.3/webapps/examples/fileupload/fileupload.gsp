<%

        import groovy.sql.Sql  
        import org.apache.commons.fileupload.FileItem

        def uploadLocation=''

        request.uploads.each {
                FileItem item = it.value

                File uploadedFile = new File(application.appPath + File.separator + item.name);
                //uploadedFile.mkdirs();
                item.write(uploadedFile);
                uploadLocation = 'File uploaded to: ' + uploadedFile.canonicalPath + ' ' + params.var

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
                         <input type="hidden" name="var" value="xxx"/>
                        <input type="submit"/>
                </form>

        </body>
</html>

