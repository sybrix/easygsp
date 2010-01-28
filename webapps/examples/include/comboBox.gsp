<%
        def comboBox(){
                def s = new StringBuilder()
                s << "<select name=\"cb\">"
                for(i in 0..5){
                        s << "<option value=\"${i}\">Item${i}</option>"
                }

                s << "</select>"

                return s
        }

%>