<html>
        <head>
                <title>Include Example</title>
        </head>

        <body>
                <%@ include 'header.gsp' %>
                <br/><br/>
                Select an Item: <%= comboBox() %>

                <br/> <br/>

        So was the comboBox
        </body>
</html>
                                       
<%@ include 'comboBox.gsp' %>