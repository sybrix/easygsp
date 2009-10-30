
<% 
	
 def comboBox(nameId, List data, valueProperty, labelProperty, selectedId, firstRow) {

                StringBuffer cb = new StringBuffer()
                cb << "\n<select name=\"${nameId}\" id=\"${nameId}\">r\n"
                if (firstRow != null){
                        cb << "<option value=\"\">$firstRow</option>"
                }
                data.each {k ->
                        if (k."$valueProperty" == selectedId){
                                cb << '<option value="'
                                cb << k."$valueProperty"
                                cb << '" selected="selected">'
                                cb << k."$labelProperty"
                                cb << '</option>\n'
                        } else{
                                cb << '<option value="'
                                cb << k."$valueProperty"
                                cb << '">'
                                cb << k."$labelProperty"
                                cb << '</option>\n'
                        }
                }
                
                cb << '</select>\n'
                println cb.toString()
        }

  	def checkBoxList(id, List data, valueProperty, labelProperty, checkedValues) {

                StringBuffer cb = new StringBuffer()
                cb << '<div class"checkBoxList">\n'
                data.each {k ->
                		cb << '<div class="checkBoxItem">'
                        if (checkedValues.contains(k."$valueProperty")){
                                cb << "<input type=\"checkbox\" name=\"${id}\" value=\""
                                cb << k."$valueProperty"
                                cb << '" checked="checked"/><span class="checkBoxItemLabel">'
                                cb << k."$labelProperty"
                                cb << '</span>\n'
                        } else {
                                cb << "<input type=\"checkbox\" name=\"${id}\" value=\""
                                cb << k."$valueProperty"
                                cb << '"/><span class="checkBoxItemLabel">'
                                cb << k."$labelProperty"
                                cb << '</span>\n'
                        }
                        cb << '</div>'
                }
                
                cb << '</div>'
                println cb.toString()
        }



%>