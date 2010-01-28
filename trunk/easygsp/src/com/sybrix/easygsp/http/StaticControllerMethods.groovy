package com.sybrix.easygsp.http

import com.sybrix.easygsp.server.EasyGServer
import com.sybrix.easygsp.util.PropertiesFile
import groovy.sql.Sql
import java.sql.SQLException
import com.sybrix.easygsp.util.Validator


public class StaticControllerMethods {
        public static addMethods(Class clazz) {
                addLogMethod(clazz)
                addLogThrowableMethod(clazz)
                addLogThrowableAndMessageMethod(clazz)
                addConsoleWriteMethod(clazz)
                addUrlDencode(clazz)
                addUrlEncode(clazz)
                addHtmlEncode(clazz)
                addLoadProperties(clazz)
                addNewSqlInstance(clazz)
                addToDbl(clazz)
                addToInt(clazz)
                addToLong(clazz)
                addIsEmailValid(clazz)
                addIsAlphaNumberic(clazz)
                addIsEmailValid(clazz)
                addIsPhoneValid(clazz)
                addIsZipCodeValid(clazz)
                addIsNumeric(clazz)
                addIsEmpty(clazz)
                addIfNull(clazz)
                // email
                // cookie
        }

        private static def addLogMethod(java.lang.Class clazz) {
                clazz.metaClass.'static'.log = {String logMessage ->
                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        LogMessage lm = new LogMessage(logMessage, app)
                        if (app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console", false)) {
                                System.out.println lm.toString()
                        }

                        EasyGSPLogger.getInstance().log(lm)
                }
        }

        private static def addLogThrowableAndMessageMethod(java.lang.Class clazz) {
                clazz.metaClass.'static'.log = {String logMessage, Throwable t ->
                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        LogMessage lm = new LogMessage(logMessage, t, app)
                        if (app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console", false)) {
                                System.out.println lm.toString()
                        }

                        EasyGSPLogger.getInstance().log(lm)
                }
        }


        private static def addLogThrowableMethod(java.lang.Class clazz) {
                clazz.metaClass.'static'.log = {Throwable t ->
                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        LogMessage logMessage = new LogMessage(t, app)
                        if (app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console", false)) {
                                System.out.println logMessage.toString()
                        }
                        EasyGSPLogger.getInstance().log(logMessage)
                }
        }



        private static def addConsoleWriteMethod(java.lang.Class clazz) {
                clazz.metaClass.'static'.console = {Object s ->
                        System.out.println(s.toString())
                }
        }

        private static def addUrlEncode(java.lang.Class clazz) {
                clazz.metaClass.'static'.encode = {String s ->
                        if (s == null)
                                return "";
                        return java.net.URLEncoder.encode(s, "UTF-8")
                }
        }

        private static def addUrlDencode(java.lang.Class clazz) {
                clazz.metaClass.'static'.decode = {String s ->
                        if (s == null)
                                return "";
                        return java.net.URLDecoder.decode(s, "UTF-8")
                }
        }

        private static def addHtmlEncode(java.lang.Class clazz) {
                clazz.metaClass.'static'.htmlEncode = {String s ->
                        StringBuffer encodedString = new StringBuffer("");
                        char[] chars = s.toCharArray();
                        for (char c: chars) {
                                if (c == '<') {
                                        encodedString.append("&lt;");
                                } else if (c == '>') {
                                        encodedString.append("&gt;");
                                } else if (c == '\'') {
                                        encodedString.append("&apos;");
                                } else if (c == '"') {
                                        encodedString.append("&quot;");
                                } else if (c == '&') {
                                        encodedString.append("&amp;");
                                } else {
                                        encodedString.append(c);
                                }
                        }
                        return encodedString.toString();
                }
        }


        private static def addLoadProperties(java.lang.Class clazz) {
                clazz.metaClass.static.loadPropertiesFile = {String fileName ->

                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        String filePath = fileName
                        if (fileName.indexOf('/') < 0 && fileName.indexOf('\\') < 0) {
                                filePath = app.appPath + File.separator + "WEB-INF" + File.separator + fileName;
                        }

                        return new PropertiesFile(filePath);
                }
        }

        private static def addNewSqlInstance(java.lang.Class clazz) {
                clazz.metaClass.static.newSqlInstance = {String dataSource ->

                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        if (dataSource == null)
                                dataSource = ''
                        else
                                dataSource += '.'
                        
                        def driver = app.getAttribute(dataSource + 'database.driver');
                        def url = app.getAttribute(dataSource + 'database.url')
                        def pwd = app.getAttribute(dataSource + 'database.password')
                        def username = app.getAttribute(dataSource + 'database.username')
                        try {
                                Sql.newInstance(url, username, pwd, driver)
                        } catch (SQLException e) {
                                throw new RuntimeException("newSqlInstance() failed. Make sure app['database.*]' properties are set and correct.", e)
                        }
                }
        }

        private static def addToInt(java.lang.Class clazz) {
                clazz.metaClass.static.toInt = {String val ->
                        if (val == null)
                                return null;
                        return Integer.parseInt(val);
                }
        }


        private static def addToLong(java.lang.Class clazz) {
                clazz.metaClass.static.toLong = {String val ->
                        if (val == null)
                                return null;
                        return Long.parseLong(val);
                }
        }

        private static def addToDbl(java.lang.Class clazz) {
                clazz.metaClass.static.toDbl = {String val ->
                        if (val == null)
                                return null;
                        return Double.parseDouble(val);
                }
        }

        private static def addIsEmailValid(java.lang.Class clazz) {
                clazz.metaClass.static.isEmail = {String val ->
                        return Validator.isEmailValid(val);
                }
        }

        private static def addIsAlphaNumberic(java.lang.Class clazz){
                clazz.metaClass.static.isAlphaNumberic = {String val ->
                        return Validator.isAlphaNumeric(val);
                }
        }

        private static def addIsZipCodeValid(java.lang.Class clazz){
                clazz.metaClass.static.isZipCode = {String val ->
                        return Validator.isZipCodeValid(val);
                }                                               
        }

        private static def addIsPhoneValid(java.lang.Class clazz){
                clazz.metaClass.static.isPhone = {String val ->
                        return Validator.isValidPhone(val);
                }
        }

        private static def addIsNumeric(java.lang.Class clazz){
                clazz.metaClass.static.isNumeric = {String val ->
                        return Validator.isNumeric(val);
                }
        }

        private static def addIsEmpty(java.lang.Class clazz){
                clazz.metaClass.static.isEmpty = {Object val ->
                        return Validator.isEmpty(val);
                }
        }

        private static def addIfNull(java.lang.Class clazz){
                clazz.metaClass.static.ifNull = {Object val, defaultVal ->
                        if (Validator.isEmpty(val))
                                return defaultVal
                        else
                                return val;
                }
        }
//                public static boolean isAlphaNumeric(String value) {
//                return ALPHA_NUMERIC_PATTERN.matcher(value).matches();
//        }
//
//        public static boolean isValidPhone(String value) {
//                return PHONE_PATTERN.matcher(value).matches();
//        }
//
//        public static boolean isZipCodeValid(String value) {
//                return ZIPCODE_PATTERN.matcher(value).matches();
//        }
                                   
}