<%

import java.text.SimpleDateFormat
def sdf = new SimpleDateFormat('MM/dd/yyyy')
def message = 'Hello World'
bind 'message', message + ' it\'s ' + sdf.format(new Date())

%> 

<html>
        <head>
                <title>Example</title>
        </head>
        <body>

                ${message}

        </body>
</html>