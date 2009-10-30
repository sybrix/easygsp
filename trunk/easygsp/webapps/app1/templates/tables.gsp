<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">
<html>
        <head>
                <title>Firebird - Tables</title>
        </head>
        <body>
                <table border="1">
                        <tr>
                                <td>
                <%

                        List t = request.getAttribute("tables")

                        t.each{out << it }

                %>
                                </td>
                        </tr>
                </table>
        </body>
</html>