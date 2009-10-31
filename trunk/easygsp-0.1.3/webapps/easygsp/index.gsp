<%
        import java.text.SimpleDateFormat

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy hh:mm a")
        String today = sdf.format(new Date())
%>
<html>
        <head>
                <title>Easy GSP</title>
        </head>
        <body>
              EasyGSP is working.
              <br/><br/>
                Today is ${today}

              <br/><br/>
                See <a href="../examples">EasyGSP examples here</a>.
        </body>
</html>