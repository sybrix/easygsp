<%

        def url = params.p.replace("/",File.separator)
        def path = app.appPath + File.separator + url

        def f = new File(path)
        def fileContent = htmlEncode(new String(f.readBytes()))

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
        <script type="text/javascript" src="scripts/shCore.js"></script>
        <script type="text/javascript" src="scripts/shBrushGroovy.js"></script>
        <script type="text/javascript" src="scripts/shBrushJava.js"></script>
        <script type="text/javascript" src="scripts/shBrushSql.js"></script>
        <script type="text/javascript" src="scripts/shBrushXml.js"></script>
        <script type="text/javascript" src="scripts/shBrushPlain.js"></script>
        <link type="text/css" rel="stylesheet" href="styles/shCore.css"/>
        <link type="text/css" rel="stylesheet" href="styles/shThemeDefault.css"/>
        <script type="text/javascript">
                SyntaxHighlighter.config.clipboardSwf = 'scripts/clipboard.swf';
                SyntaxHighlighter.all();
        </script>
</head>
<body>


<pre class="brush:groovy">
${fileContent}
</pre>

</body>
</html>