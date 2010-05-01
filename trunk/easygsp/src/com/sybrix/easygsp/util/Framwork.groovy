package com.sybrix.easygsp.util

import java.lang.reflect.InvocationTargetException
import java.beans.BeanInfo
import java.beans.PropertyDescriptor
import java.util.Map
import java.beans.Introspector
import org.codehaus.groovy.runtime.InvokerHelper

import java.util.Enumeration


class Framework {
      static String prefixEventMethods = 'on'

      static def processPage(page, List eventObjects) {
            processPage(page, eventObjects, null)
      }

      static def processPage(page, List eventObjects, Object model) {
                if (model != null){
                    def mod = populateBean(model, page.request.parameterMap)
                    def binding = page.request.servletBinding
                    binding.bind 'model', mod
                }

                def loadResult
                try {
                        loadResult = page.invokeMethod("load", null)
                        if (loadResult instanceof Boolean)
                                if (loadResult == false)
                                        return

                        bindResults(page,loadResult)

                } catch (MissingMethodException e) {
                          if(!(  e.message.indexOf('No signature of method: ') > -1
                                      &&  e.message.indexOf('.load() is applicable for argument types:' )> -1)){
                                log e
                                throw e
                         }
                }

                if (loadResult == false)
                        return

                try {
                        if (page.request.getMethod().equalsIgnoreCase('GET'))  {
                                log 'framework: calling doGet()'
                                def map = page.invokeMethod("doGet", null)
                                bindResults(page,map)

                        } else if (page.request.getMethod().equalsIgnoreCase('POST')) {
                                log 'framework: calling doPost()'
                                page.invokeMethod("doPost", null)
                        }
                } catch (MissingMethodException e) {
                        if(!(  e.message.indexOf('No signature of method: ') > -1
                                && (e.message.indexOf('.doGet() is applicable for argument types:')> -1 ||
                                        e.message.indexOf('.doPost() is applicable for argument types:' )> -1))
                          ){
                                log e
                                throw e
                        }
                }

                if (page.request.getMethod().equalsIgnoreCase('POST')) {

                        Enumeration en = page.request.getParameterNames()
                        while (en.hasMoreElements()) {
                                String s = en.nextElement()
                                //console 'param:' + s
                                if (eventObjects.contains(s)) {
                                        log "framework: ${s}() method"
                                        def map = page.invokeMethod(prefixEventMethods +  s.substring(0,1).toUpperCase() + s.substring(1,s.length()),  null)
                                        bindResults(page,map)
                                        break
                                }
                        }
                } else if (page.request.getParameter('method') != null){
                        def map = page.invokeMethod(page.request.getParameter('method'), null)
                        bindResults(page,map)
                }
        }

        def static bindResults(page, map){

                def binding = page.request.servletBinding

                if (map instanceof Map){
                        map.each{k->
                                //console 'binding ..' + k.key + ' ' + k.value
                                binding.bind k.key, k.value
                        }
                }
        }

        public static def populateBean(Object obj, Map requestParams) {
                try {
                        if (obj instanceof Class) {
                                obj = ((Class) obj).newInstance()
                        }

                        Object value = null
                        String property = null

                        BeanInfo sourceInfo = Introspector.getBeanInfo(obj.class)
                        PropertyDescriptor[] sourceDescriptors = sourceInfo.getPropertyDescriptors()

                        for (int x = 0; x < sourceDescriptors.length; x++) {
                                try {
                                        if (sourceDescriptors[x].getReadMethod() != null && sourceDescriptors[x].getWriteMethod() != null) {
                                                property = sourceDescriptors[x].getName()
                                                Class []params = sourceDescriptors[x].getWriteMethod().getParameterTypes()

                                                //log 'property: ' + property

                                                if (requestParams.get(property) != null || requestParams.get(property + '__frmwk')!= null) {
                                                        value = getValue(requestParams, property, params, obj)
                                                         //console 'property: ' + property + ' value: ' + value

                                                        if (obj instanceof GroovyObject) {
                                                                MetaClass metaClass = InvokerHelper.getMetaClass(obj)
                                                                metaClass.setProperty(obj, sourceDescriptors[x].getName(), value)
                                                        }
                                                }
                                        }
                                } catch (Exception e) {
                                        throw e
                                }
                        }

                } catch (Throwable e) {
                        throw e
                }

                return obj
        }

        private static def getValue(Map<String, String> requestParams, String property, Class[] params, Object bean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
                Object value = null
                if (params[0].equals(Integer.class) || params[0] == Integer.TYPE) {
                        value = new Integer(requestParams.get(property))
                } else if (params[0].equals(Float.class) || params[0] == Float.TYPE) {
                        value = Float.valueOf(requestParams.get(property)?:'0')
                } else if (params[0].equals(Boolean.class) || params[0] == Boolean.TYPE) {

                        if (requestParams.get(property)==null && requestParams.get(property + '__frmwk')!=null){
                               value = false
                        } else if (requestParams.get(property)!=null){
                                value = (requestParams.get(property).equals('1') || requestParams.get(property).equals('true'))
                        } else {
                                value = false
                        }

                        //console  "property:" + property  + ", requestParams.get(property):" + value
                } else if (params[0].equals(Byte.class) || params[0] == Byte.TYPE) {
                        value = Byte.valueOf(requestParams.get(property)?:'0')
                } else if (params[0].equals(Double.class) || params[0] == Double.TYPE) {
                        value = Double.valueOf(requestParams.get(property)?:'0')
                } else if (params[0].equals(Long.class) || params[0] == Long.TYPE) {
                        value = Long.valueOf(requestParams.get(property)?:'0')
                } else if (params[0].equals(Short.class) || params[0] == Short.TYPE) {
                        value = Short.valueOf( requestParams.get(property)?:'0')
                } else if (params[0].equals(Character.class) || params[0] == Character.TYPE) {
                        value = new Character((char) Integer.parseInt( requestParams.get(property)))
//                } else if (params[0].equals(Date.class)) {
//                        value = jspPage.createDate(
//                                requestParams.get(property + "_mo"),
//                                requestParams.get(property + "_dy"),
//                                requestParams.get(property + "_yr"),
//                                requestParams.get(property + "_hr", "0"),
//                                requestParams.get(property + "_mn", "0"),
//                                requestParams.get(property + "_mm", "0"),
//                                requestParams.get(property + "_ap", "AM")
//                        )
//                } else if (params[0].isEnum() &&  (params[0].getEnumConstants()[0] instanceof StringEnum || params[0].getEnumConstants()[0] instanceof NumericEnum)) {
//                                Object enumObj = params[0].getEnumConstants()[0]
//                                Method m = enumObj.getClass().getMethod("parse", String.class)
//                                value = m.invoke(bean, jspPage.getParameter(property))
                } else {
                        value = requestParams.get(property)
                }
                return value
        }

        static def checkBox(nameId, valueProperty, checked, params) {
                StringBuffer cb = new StringBuffer()
                cb << "<input type=\"checkbox\" name=\"${nameId}\" id=\"$nameId\" value=\"${valueProperty}\""
                if (checked)
                        cb << ' checked=\"checked\"'


                cb << ' ' << params
                cb << '/>'

                cb << "<input type=\"hidden\" name=\"${nameId}__frmwk\" id=\"${nameId}__frmwk\" value=\"${valueProperty}\"/>"

                return cb.toString()

        }



        static def comboBox(nameId, List data, valueProperty, labelProperty, selectedId, firstRow, params) {

                StringBuffer cb = new StringBuffer()
                cb << "\n<select name=\"${nameId}\" id=\"${nameId}\" "
				cb << params?.toString()
                cb << ">r\n"
                if (firstRow != null){
                        cb << "<option value=\"0\">$firstRow</option>"
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

                return cb.toString()
        }

        static def check(m,l){

        }
}