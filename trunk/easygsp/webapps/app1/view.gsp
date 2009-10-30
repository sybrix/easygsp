<%
        import java.text.SimpleDateFormat
        import model.*
        log 'log that'
        request.getSession(true)
%>

<html>
  <head>
    <title>${request.title} hey</title>
  </head>
  <body>
        <%=  new Profile(lastName:'lastName').lastName %> 
          ${session.creationTime}
          ${session.myname}
          ${session.time}
       <form action="index.groovy" method="POST" enctype="multipart/form-data">
		 xxxx <input type="file" name="file"/><br/>
		<input type="submit"/>
	</form>
                            jj
        <%@  include file="myinclude2.gsp" %>
  </body>
</html>
