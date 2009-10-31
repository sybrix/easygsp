<%

        int i=0;
         Thread.currentThread().getStackTrace().each {
                 cout i++;
                 cout it.methodName  + ' ' + it.lineNumber  + ' ' + it.className
         }
      // cout 'here' + Thread.currentThread().getStackTrace()[].getLineNumber();
       

%>

<html>
        <head>       
                <title>Params, Request, Session, Application Example</title>
        </head>                           
        <body>


        <form action="paramss.gsp" method="POST">
                Last Name: <input type="text" name="lastName" value=""/></br>
                You entered: ${params?.lastName}
                <input type="submit" name="submit" value="Submit"/>
                <br/><br/>
                Reading the queryString: <a href="paramss.gsp?title=easygsp&description=<%= encode('will rule the world')%>">How to read the QueryString</a> <br/><br/>
                <%
                        if (request.queryString.length()>0){
                %>
                        title: $params.title            <br/>
                        description: $params.description
                <%
                        }
                %>

        </form>
        </body>
</html>