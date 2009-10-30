package util;

import java.lang.reflect.InvocationTargetException
import java.beans.BeanInfo
import java.beans.PropertyDescriptor
import java.util.Map
import java.beans.Introspector
import org.codehaus.groovy.runtime.InvokerHelper;



import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;


class WebUtil {

        public static void process(page, List eventObjects) {
                try {
                        def map = page.invokeMethod("load", null)
                        bindResults(page,map)
                } catch (MissingMethodException e) {

                }


                try {
                        if (page.request.getMethod().equalsIgnoreCase('GET'))  {

                                def map = page.invokeMethod("get", null)
                                bindResults(page,map)

                        } else if (page.request.getMethod().equalsIgnoreCase('POST')) {
                                page.invokeMethod("post", null)
                        }
                } catch (MissingMethodException e) {

                }

                if (page.request.getMethod().equalsIgnoreCase('POST')) {
                        Enumeration en = page.request.getParameterNames();
                        while (en.hasMoreElements()) {
                                String s = en.nextElement()
                                cout 'param:' + s
                                if (eventObjects.contains(s)) {
                                        def map = page.invokeMethod(s, null)
                                        bindResults(page,map)
                                        break;
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
                                cout 'binding ..' + k.key + ' ' + k.value
                                binding.bind k.key, k.value
                        }
                }
        }

        public static Object populateBean(Object obj, Map requestParams) {
                //Object obj = null;
                try {

                        Class c = null;
                        // Get class & create instance
                        if (obj instanceof Class) {
                                obj = ((Class) obj).newInstance();
                        }

                        Object value = null;
                        String property = null;

                        BeanInfo sourceInfo = Introspector.getBeanInfo(obj.class);
                        PropertyDescriptor[] sourceDescriptors = sourceInfo.getPropertyDescriptors();

                        //Loop thru all methods
                        for (int x = 0; x < sourceDescriptors.length; x++) {
                                try {

                                        if (sourceDescriptors[x].getReadMethod() != null && sourceDescriptors[x].getWriteMethod() != null) {

                                                property = sourceDescriptors[x].getName();
                                                Class []params = sourceDescriptors[x].getWriteMethod().getParameterTypes();

                                                //log 'property: ' + property

                                                //if (params[0].isPrimitive()){
                                                if (requestParams.get(property) != null) {
                                                        value = getValue(requestParams, property, params, obj);
                                                         //log 'property: ' + property + ' value: ' + value

                                                        // Check parameters for method name */
                                                        if (obj instanceof GroovyObject) {
                                                                MetaClass metaClass = InvokerHelper.getMetaClass(obj);
                                                                metaClass.setProperty(obj, sourceDescriptors[x].getName(), value);
                                                                //cout 'here ' + obj

                                                                //} else  {
                                                                //  cout cls
                                                                //sourceDescriptors[x].getWriteMethod().invoke(obj, new Object[]{value});
                                                        }
                                                }
                                        }
                                } catch (Exception e) {
                                        //log "BeanUtil.populate failed. method=" + property + ", value=" + value, e;
                                        e.printStackTrace()
                                }
                        }

                } catch (Throwable e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                return obj;
        }

        private static Object getValue(Map<String, String> requestParams, String property, Class[] params, Object bean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
                Object value = null;
                if (params[0].equals(Integer.class) || params[0] == Integer.TYPE) {
                        value = new Integer(requestParams.get(property));
                } else if (params[0].equals(Float.class) || params[0] == Float.TYPE) {
                        value = Float.valueOf(requestParams.get(property)?:'0');
                } else if (params[0].equals(Boolean.class) || params[0] == Boolean.TYPE) {
                        value = Boolean.valueOf(requestParams.get(property)?:'false');
                } else if (params[0].equals(Byte.class) || params[0] == Byte.TYPE) {
                        value = Byte.valueOf(requestParams.get(property)?:'0');
                } else if (params[0].equals(Double.class) || params[0] == Double.TYPE) {
                        value = Double.valueOf(requestParams.get(property)?:'0');
                } else if (params[0].equals(Long.class) || params[0] == Long.TYPE) {
                        value = Long.valueOf(requestParams.get(property)?:'0');
                } else if (params[0].equals(Short.class) || params[0] == Short.TYPE) {
                        value = Short.valueOf( requestParams.get(property)?:'0');
                } else if (params[0].equals(Character.class) || params[0] == Character.TYPE) {
                        value = new Character((char) Integer.parseInt( requestParams.get(property)));
//                } else if (params[0].equals(Date.class)) {
//                        value = jspPage.createDate(
//                                requestParams.get(property + "_mo"),
//                                requestParams.get(property + "_dy"),
//                                requestParams.get(property + "_yr"),
//                                requestParams.get(property + "_hr", "0"),
//                                requestParams.get(property + "_mn", "0"),
//                                requestParams.get(property + "_mm", "0"),
//                                requestParams.get(property + "_ap", "AM")
//                        );
//                } else if (params[0].isEnum() &&  (params[0].getEnumConstants()[0] instanceof StringEnum || params[0].getEnumConstants()[0] instanceof NumericEnum)) {
//                                Object enumObj = params[0].getEnumConstants()[0];
//                                Method m = enumObj.getClass().getMethod("parse", String.class);
//                                value = m.invoke(bean, jspPage.getParameter(property));
                } else {
                        value = requestParams.get(property);
                }
                return value;
        }

}