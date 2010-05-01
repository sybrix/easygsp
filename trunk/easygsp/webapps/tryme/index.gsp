<%

        def t = new Date().getTime()

        if (!session)
                request.getSession(true)

       def s = getAppName.appPath + "\\..\\t" + session.uniqueId
       println s
       
        def f = new File(getAppName.appPath + "\\..\\t" + session.uniqueId).mkdirs()
        def d = new File("c:\\projects\\easygsp\\easygsp\\webapps\\app1\\time.gsp")

        d.write("hello")

        if (request.method.equalsIgnoreCase("POST")){
                def view = params.view
                def controller = params.controller

                //if (new File("" + session.uniqueId).
        }
%>
<html>
        <head>
                <title>EasyGSP Editor</title>
                <style type="text/css">
                        .editor{
                                width:600px;
                                height:300px
                        }
                </style>
        </head>
        <body>
                 <form action="index.gsp" method="POST">
                  <table align="center">
                        <tr>
                                <td>
                                        <div>Controller (tryme${session.uniqueId}.groovy):</div>
                                        <textarea rows="20" cols="20" name="controller" class="editor">${params.controller}</textarea>
                                </td>
                        </tr>

                        <tr>
                                <td style="padding-top:20px">
                                        <div>View (tryme${session.uniqueId}.gspx) :</div>
                                        <textarea rows="20" cols="20" name="view" class="editor">${params.view}</textarea>
                                </td>
                        </tr>

                        <tr>
                                <td align="center">
                                        <input type="submit" name="save" value="Save"> 
                                </td>
                        </tr>
                  </table>



                 </form>
        </body>
</html>

<%
        println (System.currentTimeMillis() - t) + "ms"
%>