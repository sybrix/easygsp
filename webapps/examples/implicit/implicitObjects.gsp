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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <head>
    <title>Implicit Objects</title>
  </head>
  <body>
    <h1>EasyGSP - Implicit Objects</h1>
    <hr>
    This example illustrates some of the implicit objects available 
    in the Expression Lanaguage.  The following implicit objects are 
    available (not all illustrated here):
    <ul>
      <li>request - a Map that maps request-scoped attribute names
          to their values</li>
      <li>session - a Map that maps session-scoped attribute names
          to their values</li>
      <li>application - a Map that maps application-scoped attribute
          names to their values</li>
      <li>params - a Map that maps parameter names to a single String
          parameter value</li>
      <li>headers - a Map that maps header names to a single String
          header value</li>
      <!-- li>cookies - a Map that maps cookie names to a single Cookie object.</li -->
    </ul>                                                             

    <blockquote>
      <u><b>Change Parameter</b></u>
      <form action="implicitObjects.gsp" method="GET">
	  foo = <input type="text" name="foo" value="<%= decode(params.foo)%>">
          <input type="submit">
      </form>
      <br>
      <code>
        <table border="1">
          <thead>
	    <td><b>EL Expression</b></td>
	    <td><b>Result</b></td>
	  </thead>

                <tr>
                  <td>\${params.foo}</td>
                  <td><%= decode(params.foo) %>&nbsp;</td>
                </tr>
                <tr>
                  <td>\${params['foo']}</td>
                  <td><%= decode(params['foo']) %>&nbsp;</td>
                </tr>
                <tr>
                  <td>\${headers['host']}</td>
                  <td><%= decode(headers['host']) %>&nbsp;</td>
                </tr>

                 <tr>
                  <td>\${header['accept']}</td>
                  <td><%= headers['accept']%>&nbsp;</td>
                </tr>

                <tr>
                  <td>\${header['user-agent']}</td>
                  <td><%= decode(headers['user-agent'])%>&nbsp;</td>
                </tr>

	</table>
      </code>
    </blockquote>
  </body>
</html>
