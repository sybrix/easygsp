<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">

<head profile="http://gmpg.org/xfn/11">
<meta http-equiv="Content-Type" content="text/html" />

<title><%= title == ''? '': title + ' - ' %>EasyGSP</title>


<link rel="stylesheet" href="${request.path}style.css" type="text/css" media="screen" />
<link rel="alternate" type="application/rss+xml" title="" href="" />

<!--[if IE ]>
<link rel="stylesheet" href="<?=bloginfo('template_url')?>/style-ie.css" type="text/css" media="screen" />
<script type="text/javascript">
	var png_trans = "<?=bloginfo('template_url')?>/images/transparent.gif";
</script>
<![endif]-->
<!-- Main Menu -->
	<script type="text/javascript" src="${request.path}js/jquery.min.1.2.6.js"></script>
	<script type="text/javascript" src="${request.path}js/jqueryslidemenu/jqueryslidemenu.js"></script>
        <script type="text/javascript" src="scripts/shCore.js"></script>

	<script type="text/javascript" src="${request.path}scripts/shBrushGroovy.js"></script>
	<script type="text/javascript" src="${request.path}scripts/shBrushJava.js"></script>
	<script type="text/javascript" src="${request.path}scripts/shBrushPhp.js"></script>
	<script type="text/javascript" src="${request.path}scripts/shBrushSql.js"></script>
	<script type="text/javascript" src="${request.path}scripts/shBrushXml.js"></script>
	<link type="text/css" rel="stylesheet" href="${request.path}styles/shCore.css"/>
	<link type="text/css" rel="stylesheet" href="${request.path}styles/shThemeDefault.css"/>
	<script type="text/javascript">
		SyntaxHighlighter.config.clipboardSwf = '${request.path}scripts/clipboard.swf';
		SyntaxHighlighter.all();
	</script>
        <style>
                html{
                       <% if (request.path=='../') { %>
	                background:url(${request.path}images/bgr_html2.png) repeat-x;
                       <% } else { %>
                        background:url(${request.path}images/bgr_html.png) repeat-x;

                       <% } %>
                }


                #page  {
                       <% if (request.path!='../') { %>
                        background:url(images/bgr_page.png) no-repeat;
                        <% } %>
                }

                #mainmenu ul li a:hover, #mainmenu .current_page_item a, #mainmenu .select a{
                       <% if (request.path!='../') { %>
                        background:#9d0d12;
                        <% } else { %>
                        background:#830005;
                        <% } %>
                }

        </style>
</head>

<body>

<div id="page">

        <div id="menu">

                <div id="top_rss"></div>
                        <div id="mainmenu">
                                <ul>
                                        <li class="${request.homeCssClass}"><a href="${request.path}index.gspx">Home</a></li>
                                        <li class="${request.downloadCssClass}"><a href="http://code.google.com/p/easygsp/downloads/list">Download</a></li>
                                        <li class="${request.documentationCssClass}"><a href="${request.path}documentation/index.html">Documentation</a></li>
                                        <li class="${request.communityCssClass}"><a href="http://code.google.com/p/easygsp/issues/list">Report Bugs</a></li>
                                </ul>
                        </div>
                </div>

                <div id="header">
                        <div id="header_title">
                                <img src="${request.path}images/easygsp.gif" alt="" >
                        </div>
                        <div id="header_right">
                                
                    </div>
                </div>

                <% if (showEasyGSPQuestion == true) { %>
                <div id="board">
                        <div id="board_post">

                                <h2>What is EasyGSP ?</h2>
                                <h3>
                                        EasyGSP allows you build dynamic websites using the Groovy programming language with LightTPD, Apache or any web server that supports the SCGI protocol. <a href="faq.gsp"> Read more...</a>
                                </h3>

                        </div>
                </div>
                <% }  %>

<div id="body">

	<div id="body_left">
    	<div id="body_left_content">