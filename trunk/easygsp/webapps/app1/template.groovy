
<%
        import java.text.SimpleDateFormat
        import model.*
        session.myname = 'David Smith'
        session.time = new Date()
        log 'log this'
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


  </body>
</html>
