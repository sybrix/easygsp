<%="an error occurred"%>

<html>
<head>
        <title>500 error</title>
</head>
<body>
<table border="1">
        <tr>
                <td>\${error}</td><td>${error}</td>
        </tr>
        <tr>
                <td valign="top">\${errorMessage}</td><td>${errorMessage}</td>
        </tr>
        <tr>
                <td valign="top">\${errorScript}</td><td>${errorScript}</td>
        </tr>
        <tr>
                <td valign="top">\${lineNumber}</td><td>${lineNumber}</td>
        </tr>
</table>
</body>
</html>