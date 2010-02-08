<%
        import groovy.sql.Sql

        def sql = Sql.newInstance("jdbc:mysql://localhost:3306/sybrixApps","root", "root", "com.mysql.jdbc.Driver")

        /*

        CREATE TABLE `profile` (
        `profile_id` int(11) NOT NULL AUTO_INCREMENT,
        `last_name` varchar(20) DEFAULT NULL,
        `first_name` varchar(20) DEFAULT NULL,
        PRIMARY KEY (`profile_id`)
        ) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

        INSERT INTO `profile` (`profile_id`, `last_name`, `first_name`) VALUES
        (1,'Smith','David'),
        (2,'Woods','Tiger'),
        (3,'Blizter','Wolf');
        COMMIT;
         */
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