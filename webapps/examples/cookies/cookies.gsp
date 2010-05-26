<%
        if (request.method == 'POST'){
                setCookie('easygsp_cookie', params.val, 30) // expires in 30 days
                render 'Cookie Saved' 
        }
%>

<html>
        <head>
                <title>Database Example</title>
        </head>
        <body>

                <form action="cookies.gsp" method="post">
                        Enter value, it will be saved to a cookie named "easygsp_cookie" </br>
                        <input type="text" name="val" value="${params.val?:''}"/>
                        <input type="submit" name="submit"  value="Save"/>
                </form>
        </body>
</html>

