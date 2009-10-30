println """
<html>
        <head>
               <title>500 - Internal Service Error</title>
        </head>
        <body>
                Custom 500 - Internal Service Error  <br/><br/>
                
                <div style="font-family:arial, verdana;color:firebrick;padding-left:50px;font-size:.90em">
                        script : ${requestAttr.script} <br/><br/>

                        ${requestAttr.errorMessage}
                </div>

                <br/><br/><font size="1" face="arial">Groovy Script Server 1.0</font>
        </body>
</html>"""