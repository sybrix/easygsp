<%

        if (request.getMethod().equalsIgnoreCase("POST")){
                redirect 'http://www.google.com'

        }
%>
<html>
        <head>
                <title>Redirect Example</title>
        </head>
        <body>

                <form action="redirect.gsp" method="post">
                        <input type="submit" name="btn" value="Post & Redirect to Google.com">
                </form>


                <br/>

        
        </body>
</html>