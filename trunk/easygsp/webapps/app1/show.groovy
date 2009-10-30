
//response.setStatus(401, "Unauthorized")
//response.setHeader("WWW-Authenticate", "Basic realm=\"My Realm\"")



println """
<html><head>
<title>Groovlets 101</title>
</head>
<body>
<h1>App1</h1>
<p>    
Welcome to Groovlets 101. As you can see
this Groovlet is fairly simple.
</p>
<p>
This course is being run on the following servlet container: </br>
${application.getServerInfo()}

</p>
</body>
</html>
"""