<%
        import groovy.sql.Sql

        // look no database, setup
        def sql = newSqlInstance() // implicit method, see documentation
%>

<html>
        <head>
                <title>Database Example</title>
        </head>
        <body>
                <table align="center" border="1">
                        <tr>
                                <td>Id</td>
                                <td>LastName</td>
                                <td>FirstName</td>
                        </tr>
                        <% sql.eachRow("select profile_id profileId, last_name lastName, first_name firstName from profile") {profile->  %>
                                <tr>
                                        <td>${profile.profileId}</td>
                                        <td>${profile.lastName}</td>
                                        <td>${profile.firstName}</td>
                                </tr>
                        <% } %>
                </table>
        </body>
</html>


