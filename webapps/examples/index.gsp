<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
        <meta name="GENERATOR" content="Mozilla/4.61 [en] (WinNT; I) [Netscape]">
        <meta name="Author" content="Anil K. Vijendran">
        <title>Easy GSP Examples</title>

</head>
<body bgcolor="#FFFFFF">
<b><font face="Arial, Helvetica, sans-serif"><font size=+2>EasyGSP
        Samples</font></font></b>

<p>This is a collection of samples demonstrating the usage of different
        parts of the EasyGSP engine.

<p>These examples will only work when these pages are being served by a
        scgi engine; of course, we recommend
        <a href="http://www.lighttpd.net/">LightTPD</a> as the HTTP server.
        They will not work if you are viewing these pages via a
        "file://..." URL.

<p>To navigate your way through the examples, the following icons will
        help:
        <br>&nbsp;
<table BORDER=0 CELLSPACING=5 WIDTH="85%">
        <tr VALIGN=TOP>
                <td WIDTH="30"><img SRC="images/execute.gif"></td>

                <td>Execute the example</td>
        </tr>

        <tr VALIGN=TOP>
                <td WIDTH="30"><img SRC="images/code.gif" height=24 width=24></td>

                <td>Look at the source code for the example</td>
        </tr>

        <!--<tr VALIGN=TOP>
        <td WIDTH="30"><img SRC="images/read.gif" height=24 width=24></td>

        <td>Read more about this feature</td>
        -->

        </tr>
        <tr VALIGN=TOP>
                <td WIDTH="30"><img SRC="images/return.gif" height=24 width=24></td>

                <td>Return to this screen</td>
        </tr>
</table>

<p>Tip: For session scoped beans to work, the cookies must be enabled.
        This can be done using browser options.
        <br>&nbsp;
        <br>
        <br>
<table BORDER=0 CELLSPACING=5 WIDTH="85%">

        <tr valign=TOP>
                <td>File Upload</td>
                <td valign=TOP width="30%"><a href="fileupload/fileupload.gsp"><img src="images/execute.gif" hspace=4 border=0 align=top></a><a href="fileupload/fileupload.gsp">Execute</a></td>

                <td width="30%"><a href="viewsource.gsp?p=<%= encode('fileupload/fileupload.gsp') %>"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a>
                        <a href="viewsource.gsp?p=<%= encode('fileupload/fileupload.gsp') %>" target="_blank">Source</a></td>
        </tr>
        <tr valign=TOP>
                <td>Implicit Objects</td>
                <td valign=TOP width="30%"><a href="implicit/implicitObjects.gsp?foo=bar"><img src="images/execute.gif" hspace=4 border=0 align=top></a><a href="implicit/implicitObjects.gsp?foo=bar">Execute</a></td>

                <td width="30%"><a href="viewsource.gsp?p=<%= encode('implicit/implicitObjects.gsp') %>" target="_blank"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a><a href="viewsource.gsp?p=<%= encode('implicit/implicitObjects.gsp') %>" target="_blank">Source</a></td>
        </tr>

        <tr VALIGN=TOP>
                <td>Numberguess&nbsp;</td>

                <td VALIGN=TOP WIDTH="30%"><a href="num/numguess.gsp"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP></a><a href="num/numguess.gsp">Execute</a></td>

                <td WIDTH="30%"><a href="viewsource.gsp?p=<%= encode('num/numguess.gsp') %>"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a><a href="viewsource.gsp?p=<%= encode('num/numguess.gsp') %>">Source</a></td>
        </tr>

        <tr VALIGN=TOP>
                <td>Date&nbsp;</td>

                <td VALIGN=TOP WIDTH="30%"><a href="dates/date.gsp"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP></a><a href="dates/date.gsp">Execute</a></td>

                <td WIDTH="30%"><a href="viewsource.gsp?p=<%= encode('dates/date.gsp') %>"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a><a href="viewsource.gsp?p=<%= encode('dates/date.gsp') %>">Source</a></td>
        </tr>

        <tr VALIGN=TOP>
                <td>Snoop</td>

                <td WIDTH="30%"><a href="snp/snoop.gsp"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP></a><a href="snp/snoop.gsp">Execute</a></td>

                <td WIDTH="30%"><a href="viewsource.gsp?p=<%= encode('snp/snoop.gsp') %>"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a><a href="viewsource.gsp?p=<%= encode('snp/snoop.gsp') %>">Source</a></td>
        </tr>

        <tr VALIGN=TOP>
                <td>ErrorPage(500)&nbsp;</td>

                <td WIDTH="30%"><a href="errors/index.gsp"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP></a><a href="errors/index.gsp">Execute</a></td>

                <td WIDTH="30%"><a href="viewsource.gsp?p=<%= encode('errors/index.gsp') %>"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a><a href="viewsource.gsp?p=<%= encode('errors/index.gsp') %>">Source</a></td>
        </tr>
        <tr VALIGN=TOP>
                <td>ErrorPage(404)&nbsp;</td>

                <td WIDTH="30%"><a href="errors/index2.gsp"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP></a><a href="errors/index2.gsp">Execute</a></td>

                <td WIDTH="30%"></td>
        </tr>


        <tr VALIGN=TOP>
                <td>Include&nbsp;</td>

                <td VALIGN=TOP WIDTH="30%"><a href="include/include.gsp"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP></a><a href="include/include.gsp">Execute</a></td>

                <td WIDTH="30%"><a href="viewsource.gsp?p=<%= encode('include/include.gsp') %>"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a><a href="include.gsp?p=<%= encode('include/include.gsp') %>">Source</a></td>
        </tr>

        <tr VALIGN=TOP>
                <td>Forward&nbsp;</td>

                <td VALIGN=TOP WIDTH="30%"><a href="forward/forward.gsp"><img SRC="images/execute.gif" HSPACE=4 BORDER=0 align=TOP></a><a href="forward/forward.gsp">Execute</a></td>

                <td WIDTH="30%"><a href="viewsource.gsp?p=<%= encode('forward/forward.gsp') %>"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a><a href="viewsource.gsp?p=<%= encode('forward/forward.gsp') %>">Source</a></td>
        </tr>
        <tr valign=TOP>
                <td>Redirect</td>
                <td valign=TOP width="30%"><a href="redirect/redirect.gsp"><img src="images/execute.gif" hspace=4 border=0 align=top></a><a href="redirect/redirect.gsp">Execute</a></td>

                <td width="30%"><a href="viewsource.gsp?p=<%= encode('redirect/redirect.gsp') %>"><img SRC="images/code.gif" HSPACE=4 BORDER=0 height=24 width=24 align=TOP></a>
                        <a href="viewsource.gsp?p=<%= encode('redirect/redirect.gsp') %>" target="_blank">Source</a></td>
        </tr>
</table>


</body>
</html>
