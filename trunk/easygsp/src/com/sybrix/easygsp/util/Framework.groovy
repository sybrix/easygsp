package com.sybrix.easygsp.util

import java.beans.BeanInfo
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.InvocationTargetException
import org.codehaus.groovy.runtime.InvokerHelper

public class Framework {
        static String prefixEventMethods = 'on'

        static def processPage(page, List eventObjects) {
                processPage(page, eventObjects, null)
        }

        static def processPage(page, List eventObjects, Object model) {
                boolean didDoPost = false

                if (model != null) {
                        def mod = populateBean(model, page.request.parameterMap)
                        def binding = page.request.servletBinding
                        binding.bind 'model', mod
                }

                def loadResult
                try {
                        loadResult = page.invokeMethod("load", null)
                        if (loadResult instanceof Boolean) {
                                if (loadResult == false)
                                        return
                        } else if (loadResult instanceof String) {
                                render(loadResult)
                                return
                        } else {
                                bindResults(page, loadResult)
                        }
                } catch (MissingMethodException e) {
                        if (!(e.message.indexOf('No signature of method: ') > -1
                                && e.message.indexOf('.load() is applicable for argument types:') > -1)) {
                                //log e
                                throw e
                        }
                }

                if (loadResult == false)
                        return

                try {
                        if (page.request.getMethod().equalsIgnoreCase('GET')) {
                                //log 'framework: calling doGet()'
                                def map = page.invokeMethod("doGet", null)
                                bindResults(page, map)

                        } else if (page.request.getMethod().equalsIgnoreCase('POST')) {
                                //log 'framework: calling doPost()'
                                page.invokeMethod("doPost", null)
                                didDoPost = true
                        }
                } catch (MissingMethodException e) {
                        if (!(e.message.indexOf('No signature of method: ') > -1
                                && (e.message.indexOf('.doGet() is applicable for argument types:') > -1 ||
                                e.message.indexOf('.doPost() is applicable for argument types:') > -1))
                        ) {
                                // log e
                                throw e
                        }
                }

                if (page.request.getMethod().equalsIgnoreCase('POST')) {

                        Enumeration en = page.request.getParameterNames()
                        boolean found = false
                        while (en.hasMoreElements()) {
                                String s = en.nextElement()

                                if (s.endsWith('.x') || s.endsWith('.y')) {
                                        s = s.substring(0, s.length() - 2)
                                }

                                if (eventObjects.contains(s)) {
                                        //log "framework: ${s}() method"
                                        def map = page.invokeMethod(prefixEventMethods + s.substring(0, 1).toUpperCase() + s.substring(1, s.length()), null)
                                        bindResults(page, map)
                                        found = true
                                        break
                                }
                        }

                        if (!found && !didDoPost) {
                                // if post but no item clicked, default to first item in list
                                if (eventObjects.size() > 0) {
                                        String s = eventObjects[0]
                                        def map = page.invokeMethod(prefixEventMethods + s.substring(0, 1).toUpperCase() + s.substring(1, s.length()), null)
                                        bindResults(page, map)
                                } else {
                                        throw new RuntimeException("No event method found for post.  Check form's submit button name.")
                                }
                        }

                } else if (page.request.getParameter('method') != null) {
                        def map = page.invokeMethod(page.request.getParameter('method'), null)
                        bindResults(page, map)
                }
        }

        def static bindResults(page, map) {

                def binding = page.request.servletBinding

                if (map instanceof Map) {
                        map.each {k ->
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
                                                Class[] params = sourceDescriptors[x].getWriteMethod().getParameterTypes()

                                                //println 'property: ' + property
                                                if (requestParams.get(property) != null || requestParams.get(property + '__frmwk') != null) {
                                                        value = getValue(requestParams, property, params, obj)
                                                        // println 'set property: ' + property + ' value: ' + value

                                                        if (obj instanceof GroovyObject && obj instanceof com.sybrix.easygsp.db.Model) {
                                                                ((com.sybrix.easygsp.db.Model) obj).setProperty(sourceDescriptors[x].getName(), value)
                                                        } else if (obj instanceof GroovyObject) {
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

        static boolean propertyExist(Class clazz, String propertyName) {
                MetaBeanProperty metaProperty = clazz.metaClass.getMetaProperty(propertyName)
                if (metaProperty != null) {
                        return true
                } else {
                        return false
                }
        }


        private static def getValue(Map<String, String> requestParams, String property, Class[] params, def bean) throws NoSuchMethodException,
                InvocationTargetException, IllegalAccessException {
                Object value = null

                if (!bean.metaClass.getMetaProperty('errors')) {
                        bean.metaClass.errors = [:]
                        bean.metaClass.hasErrors = {
                                return delegate.errors.size() > 0
                        }
                }

                try {
                        if (params[0].equals(Integer.class) || params[0] == Integer.TYPE) {
                                value = new Integer(requestParams.get(property))
                        } else if (params[0].equals(Float.class) || params[0] == Float.TYPE) {
                                value = Float.valueOf(requestParams.get(property) ?: '0')
                        } else if (params[0].equals(Boolean.class) || params[0] == Boolean.TYPE) {

                                if (requestParams.get(property) == null && requestParams.get(property + '__frmwk') != null) {
                                        value = false
                                } else if (requestParams.get(property) != null) {
                                        value = (requestParams.get(property).equals('1') || requestParams.get(property).equals('true'))
                                } else {
                                        value = false
                                }

                                //console  "property:" + property  + ", requestParams.get(property):" + value
                        } else if (params[0].equals(Byte.class) || params[0] == Byte.TYPE) {
                                value = Byte.valueOf(requestParams.get(property) ?: '0')
                        } else if (params[0].equals(Double.class) || params[0] == Double.TYPE) {
                                value = Double.valueOf(requestParams.get(property) ?: '0')
                        } else if (params[0].equals(BigDecimal.class)) {
                                value = new BigDecimal(requestParams.get(property) ?: '0')
                        } else if (params[0].equals(Long.class) || params[0] == Long.TYPE) {
                                value = Long.valueOf(requestParams.get(property) ?: '0')
                        } else if (params[0].equals(Short.class) || params[0] == Short.TYPE) {
                                value = Short.valueOf(requestParams.get(property) ?: '0')
                        } else if (params[0].equals(Character.class) || params[0] == Character.TYPE) {
                                value = new Character((char) Integer.parseInt(requestParams.get(property)))
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
                } catch (Exception e) {
                        def v = requestParams.get(property)
                        bean.errors.put(property, e);
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

        static def comboBox(nameId, data, valueProperty, labelProperty, selectedId, firstRow, params) {

                StringBuffer cb = new StringBuffer()
                cb << "\n<select name=\"${nameId}\" id=\"${nameId}\" "
                cb << params?.toString()
                cb << ">\r\n"

                if (firstRow != null) {
                        cb << "<option value=\"0\">$firstRow</option>"
                }

                if ((data instanceof List) && valueProperty != null && labelProperty !=null) {
                        data.each {k ->
                                if (k."$valueProperty" == selectedId) {
                                        cb << '<option value="'
                                        cb << k."$valueProperty"
                                        cb << '" selected="selected">'
                                        cb << k."$labelProperty"
                                        cb << '</option>\n'
                                } else {
                                        cb << '<option value="'
                                        cb << k."$valueProperty"
                                        cb << '">'
                                        cb << k."$labelProperty"
                                        cb << '</option>\n'
                                }
                        }
                } else if (data instanceof List) {
                         def i = 0
                        data.each {v ->
                                if (v == selectedId) {
                                        cb << '<option value="'
                                        cb << v
                                        cb << '" selected="selected">'
                                        cb << v
                                        cb << '</option>\n'
                                } else {
                                        cb << '<option value="'
                                        cb << v
                                        cb << '">'
                                        cb << v
                                        cb << '</option>\n'
                                }
                        }
                } else {
                        def i = 0
                        data.each {k, v ->
                                if (k == selectedId) {
                                        cb << '<option value="'
                                        cb << k
                                        cb << '" selected="selected">'
                                        cb << v
                                        cb << '</option>\n'
                                } else {
                                        cb << '<option value="'
                                        cb << k
                                        cb << '">'
                                        cb << v
                                        cb << '</option>\n'
                                }
                        }
                }

                cb << '</select>\n'

                return cb.toString()
        }

        static def check(m, l) {

        }

        static def doPagination(thisPage) {
                def binding = thisPage.binding
                def (v, q) = getPageParams1(thisPage)

                v.each {key, val ->
                        binding."$key" = val
                }

                def queryString = new StringBuffer()

                q.each {key, val ->
                        queryString << "&${key}=${val}"
                }

                def currentPage = binding.oldPage
                def requestedPage = binding.page
                def pageSize = binding.pageSize
                def sortColumn = binding?.sortColumn
                def sortOrder = binding.sortOrder
                def totalCount = binding.data.totalCount
                def pageCount = binding.data.pageCount
                def pageName = thisPage.request.requestURL

                def beginPage = 1
                def endPage = pageCount
                def previous = (requestedPage - 1) < 1 ? 1 : requestedPage - 1
                def next = (requestedPage + 1) > pageCount ? pageCount : requestedPage + 1

                def out = new StringBuffer()

                out << '<ul class=\"pageNav\">'

                out << "<li><a href=\"${pageName}?sortColumn=${sortColumn}&sortOrder=${sortOrder}&page=${previous}&p=${currentPage}${queryString}\">Previous</a></li>"

                def range = getRange(requestedPage, pageCount)

                if (range.leapBack > 0) {
                        if (range.leapBack != 1)
                                out << "<li><a href=\"${pageName}?sortColumn=${sortColumn}&sortOrder=${sortOrder}&page=1&p=${currentPage}${queryString}\">1</a></li>"

                        out << "<li><a href=\"${pageName}?sortColumn=${sortColumn}&sortOrder=${sortOrder}&page=${range.leapBack}&p=${currentPage}${queryString}\">${range.leapBack}</a></li>"
                        out << "<li class=\"currentPage\">...</li>"
                }

                (range.start..range.end).each {i ->
                        if (i == requestedPage)
                                out << "<li class=\"currentPage\">${i}</li>"
                        else
                                out << "<li><a href=\"${pageName}?sortColumn=${sortColumn}&sortOrder=${sortOrder}&page=${i}&p=${currentPage}${queryString}\">$i</a></li>"
                }

                if (range.leap > 0) {
                        out << "<li class=\"currentPage\">...</li>"
                        out << "<li><a href=\"${pageName}?sortColumn=${sortColumn}&sortOrder=${sortOrder}&page=${range.leap}&p=${currentPage}${queryString}\">${range.leap}</a></li>"

                        if (range.leap != pageCount)
                                out << "<li><a href=\"${pageName}?sortColumn=${sortColumn}&sortOrder=${sortOrder}&page=${pageCount}&p=${currentPage}${queryString}\">${pageCount}</a></li>"
                }

                out << "<li><a href=\"${pageName}?sortColumn=${sortColumn}&sortOrder=${sortOrder}&page=${next}&p=${currentPage}${queryString}\">Next</a></li>"

                out << '</ul>'

                return out.toString()
        }

        static def getRange(currentPage, pageCount) {
                def slideWidth = 5
                int page = currentPage
                float m = slideWidth;
                int halfOfRange = (int) Math.floor(m / 2f);
                int rangeEnd = slideWidth;
                //int range[] = new int[slideWidth];
                int start = 1;

                start = page - halfOfRange;
                rangeEnd = page + halfOfRange;

                if (pageCount < slideWidth) {
                        start = 1;
                        rangeEnd = pageCount==0?1:pageCount;
                        //range = new int[rangeEnd];
                } else if (rangeEnd > pageCount) {
                        rangeEnd = pageCount;
                        start = rangeEnd - slideWidth + 1;
                } else if (start < 1) {
                        start = 1;
                        rangeEnd = slideWidth;

                        if (rangeEnd > pageCount)
                                rangeEnd = pageCount;
                }

                def range = [:]
                range.start = start
                range.end = rangeEnd
                range.leap = 0
                range.leapBack = 0
                def leapAmount = (pageCount * 0.30f).toInteger()

                def pageCountRangeEndDiff = pageCount - range.end
                if (range.start - leapAmount > 1)
                        range.leapBack = range.start - leapAmount
                else if (((range.start - 1) / 2) > 1)
                        range.leapBack = ((range.start - 1) / 2).toInteger()

                if ((range.end + leapAmount) < pageCount) {
                        range.leap = range.end + leapAmount
                } else if (pageCountRangeEndDiff > 0 && pageCountRangeEndDiff <= leapAmount) {
                        range.leap = range.end + pageCountRangeEndDiff
                }

                return range;
        }


        static def getPageParams1(tp) {
                def param = tp.params
                def sortColumn = param.sortColumn
                def sortOrder = 'ASC'
                def page = new Integer(param.page ?: '1')
                def pageSize = new Integer(param.pageSize ?: '20')
                def oldPage = new Integer(param.p ?: page.toString())

                if (param.sortColumn != null) {
                        sortColumn = param.sortColumn
                        if (oldPage == page) {
                                sortOrder = param.sortOrder ?: 'DESC';
                        } else {
                                sortOrder = param.sortOrder;
                        }
                } else {
                        sortColumn = tp.binding?.sortColumn
                        sortOrder = tp.binding.sortOrder
                }

                def p = [sortColumn:sortColumn, sortOrder: sortOrder, page: page, pageSize: pageSize, oldPage: oldPage, p: page]

                def queryStringMap = [:]
                param.each {k, v ->
                        if (!p.containsKey(k)) {
                                queryStringMap.put(k, v)
                        }
                }

                [p, queryStringMap]
        }


        static def column(thisPage, col, columnLabel) {
                def url = thisPage.request.requestURL
                def sortOrder = getSortOrder(thisPage.params)
                def queryString=""

                thisPage.params.each {key, val ->
                        if (!key.equals('sortOrder') && !key.equals('sortColumn')){
                                queryString += "&${key}=${val}"
                               // println "&${key}=${val}"
                        }
                }

                """<a href="${url}?sortColumn=${col}&sortOrder=${sortOrder == 'ASC' ? 'DESC' : 'ASC'}${queryString}">${columnLabel}</a>"""
        }

        static def defaultPageSize = '10'

        static def getSortOrder(params) {
                def sortColumn = params.sortColumn
                def sortOrder = 'ASC'
                def page = new Integer(params.page ?: '1')
                def pageSize = new Integer(params.pageSize ?: defaultPageSize)
                def oldPage = new Integer(params.p ?: page.toString())


                if (params.sortColumn != null) {
                        sortColumn = params.sortColumn
                        if (oldPage == page) {
                                sortOrder = params.sortOrder ?: 'DESC';
                        } else {
                                sortOrder = params.sortOrder;
                        }
                }

                sortOrder
        }

        static def getOrderBy(params) {
                def sortColumn = params.sortColumn
                def sortOrder = 'ASC'

                def page = new Integer(params.page ?: '1')
                def pageSize = new Integer(params.pageSize ?: defaultPageSize)
                def oldPage = new Integer(params.p ?: page.toString())

                if (params.sortColumn != null) {
                        sortColumn = params.sortColumn
                        if (oldPage == page) {
                                sortOrder = params.sortOrder ?: 'DESC';
                        } else {
                                sortOrder = params.sortOrder;
                        }
                }

                sortColumn + ' ' + sortOrder
        }

        static def copyProperties(def source, def target) {
                try {
                        Object value = null
                        String property = null

                        BeanInfo sourceInfo = Introspector.getBeanInfo(source.class)

                        PropertyDescriptor[] sourceDescriptors = sourceInfo.getPropertyDescriptors()

                        for (int x = 0; x < sourceDescriptors.length; x++) {
                                try {

                                        property = sourceDescriptors[x].getName()
                                        //println 'set property: ' + property
                                        if (sourceDescriptors[x].getWriteMethod() == null)
                                                continue

                                        Class[] params = sourceDescriptors[x].getWriteMethod().getParameterTypes()

                                        //println 'property: ' + property

                                        value = source."$property"
                                        //println 'set property: ' + property + ' value: ' + value

                                        MetaClass metaClass = InvokerHelper.getMetaClass(target)
                                        metaClass.setProperty(target, sourceDescriptors[x].getName(), value)

                                } catch (Exception e) {
                                        throw e
                                }
                        }

                } catch (Throwable e) {
                        throw e
                }
        }
}