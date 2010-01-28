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


<body bgcolor="white">

<%
        def cal = new dates.JspCalendar();
%>
<font size=4>
<ul>
<li>	Day of month: is  ${cal.dayOfMonth}
        <li>	Year: is  ${cal.year}
        <li>	Month: is  ${cal.month}
        <li>	Time: is  ${cal.time}
        <li>	Date: is  ${cal.date}
        <li>	Day: is  ${cal.day}
        <li>	Day Of Year: is  ${cal.dayOfYear}
        <li>	Week Of Year: is ${cal.weekOfYear}
        <li>	era: is  ${cal.era}
        <li>	DST Offset: ${cal.DSTOffset}
        <li>	Zone Offset: is  ${cal.zoneOffset}

</ul>
</font>

</body>
</html>
