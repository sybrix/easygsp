<html>
        <head>
                <title>
                Reading Properties File Example
                </title>
        </head>

        File Contents:<br/>
<%

        Properties p  = new Properties()
        p.load(new FileInputStream(application.appPath + File.separator + 'example.properties'))


        for(def key: p.propertyNames()){
                def val = p.get(key)
%>
                $key = $val <br/>
<%
        }

%>
</html>
