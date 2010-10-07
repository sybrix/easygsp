<%
      def t 
      if (params.first) {
              t = """
                <html>
                        <head>
                                <title>
                                &lt;%@ block title %&gt; &lt;%@ endblock %&gt;
                                </title>
                        </head>
                        <body>
                                &lt;%@ block body %&gt; &lt;%@ endblock %&gt;
                        </body>
                </html>
              """
        } else {
              t = """
                <html>
                        <head>
                                <title>
                                &lt;%@ block title  %&gt; &lt;%@ endblock %&gt;
                                </title>
                        </head>
                        <body>
                                modified &lt;%@ block body %&gt; &lt;%@ endblock %&gt;
                        </body>
                </html>
              """        
        }
        
        new File(app.appPath + File.separator + 'views' + File.separator + 'tempTemplate.gsp').write(t.replaceAll('&lt;','<').replaceAll('&gt;','>'))
%>