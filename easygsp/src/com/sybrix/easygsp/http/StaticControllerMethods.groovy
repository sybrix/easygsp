package com.sybrix.easygsp.http

import com.sybrix.easygsp.server.EasyGServer
import com.sybrix.easygsp.util.PropertiesFile
import groovy.sql.Sql
import java.sql.SQLException
import com.sybrix.easygsp.util.Validator
import com.sybrix.easygsp.util.Hash
import java.text.DecimalFormat

import com.sybrix.easygsp.exception.SendEmailException
import com.sybrix.easygsp.email.Email
import com.sybrix.easygsp.email.EmailService
import com.sybrix.easygsp.logging.LogMessage
import com.sybrix.easygsp.logging.EasyGSPLogger
import java.text.SimpleDateFormat
import com.google.gson.Gson
import java.lang.reflect.Type
import com.sybrix.easygsp.util.FileUtil
import java.sql.Timestamp


public class StaticControllerMethods {
        static SimpleDateFormat sdf_short = new SimpleDateFormat("MM/dd/yyyy")
        static SimpleDateFormat sdf_long = new SimpleDateFormat("EEEE, MMMM dd, yyyy")

        static {

        }

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
                addIsAlphaNumeric(clazz)
                addIsEmailValid(clazz)
                addIsPhoneValid(clazz)
                addIsZipCodeValid(clazz)
                addIsNumeric(clazz)
                addIsEmpty(clazz)
                addIfNull(clazz)
                addMD5(clazz)
                addSHA1(clazz)
                addFormatBigDecimal(clazz)
                addFormatDate(clazz)
                addFormatDouble(clazz)
                addFormatMoney(clazz)
                addProperties(clazz)
                addSendEmail(clazz)
                addToJson(clazz)
                addFromJson(clazz)
                addCopy(clazz)
                addDelete(clazz)
                addNow(clazz)
        }

        private static def addNow(java.lang.Class clazz) {
                 clazz.metaClass.'static'.now = {
                         return new Timestamp(System.currentTimeMillis());
                 }
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


        public static def addLogAppThrowableAndMessageMethod(ServletContextImpl app, String logMessage, Throwable t) {
                LogMessage lm = new LogMessage(logMessage, t, app)
                if (app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console", false)) {
                        System.out.println lm.toString()
                }

                EasyGSPLogger.getInstance().log(lm)
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
                        return htmlEncode(s);
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
/*
        public static void registerEvents(Object page) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
                Method methods[] = page.getClass().getMethods();
                for (Method method : methods) {
                        //public void registerEvent(String objectName, String function, boolean isButton) {
                        Event eventAnnotation = method.getAnnotation(Event.class);
                        if (eventAnnotation != null) {
                                String names[] = eventAnnotation.source().split(",");
                                for (String name : names) {
                                        invokeMethod(page, "registerEvent", new Class[]{String.class, String.class, Boolean.class}, name.trim(), method.getName(), new Boolean(false));
                                }
                        }
                }
        }
  */

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
                clazz.metaClass.static.isEmailValid = {String val ->
                        return Validator.isEmailValid(val);
                }
        }

        private static def addIsAlphaNumeric(java.lang.Class clazz) {
                clazz.metaClass.static.isAlphaNumeric = {String val ->
                        return Validator.isAlphaNumeric(val);
                }
        }

        private static def addIsZipCodeValid(java.lang.Class clazz) {
                clazz.metaClass.static.isZipCode = {String val ->
                        return Validator.isZipCodeValid(val);
                }
        }

        private static def addIsPhoneValid(java.lang.Class clazz) {
                clazz.metaClass.static.isPhone = {String val ->
                        return Validator.isValidPhone(val);
                }
        }

        private static def addIsNumeric(java.lang.Class clazz) {
                clazz.metaClass.static.isNumeric = {Object val ->
                        return Validator.isNumeric(val.toString());
                }
        }

        private static def addIsEmpty(java.lang.Class clazz) {
                clazz.metaClass.static.isEmpty = {Object val ->
                        return Validator.isEmpty(val);
                }
        }

        private static def addIfNull(java.lang.Class clazz) {
                clazz.metaClass.static.ifNull = {Object val, defaultVal ->
                        if (Validator.isEmpty(val))
                                return defaultVal
                        else
                                return val;
                }
        }

        private static def addMD5(java.lang.Class clazz) {
                clazz.metaClass.static.MD5 = {String val ->
                        return Hash.MD5(val)
                }
        }

        private static def addSHA1(java.lang.Class clazz) {
                clazz.metaClass.static.SHA1 = {String val ->
                        return Hash.SHA1(val)
                }
        }

        private static def addFormatBigDecimal(java.lang.Class clazz) {
                clazz.metaClass.static.format = {BigDecimal val, String pattern ->
                        DecimalFormat decimalFormat = new DecimalFormat(pattern)
                        return decimalFormat.format(val.toDouble())
                }
        }

        private static def addFormatDouble(java.lang.Class clazz) {
                clazz.metaClass.static.format = {java.lang.Double val, String pattern ->
                        DecimalFormat decimalFormat = new DecimalFormat(pattern)
                        return decimalFormat.format(val)
                }
        }

        private static def addFormatMoney(java.lang.Class clazz) {
                clazz.metaClass.static.formatMoney = {Object val ->
                        DecimalFormat moneyFormatter = DecimalFormat.getCurrencyInstance()
                        if (val instanceof BigDecimal)
                                return moneyFormatter.format(val.toDouble())
                        else if (val instanceof Double)
                                return moneyFormatter.format(val)
                        else if (val instanceof String)
                                return moneyFormatter.format(new Double(val))
                }
        }

        private static def addFormatDate(java.lang.Class clazz) {
                clazz.metaClass.static.formatDate = {Object val, Object format ->
                        def dt
                        if (val instanceof java.sql.Date){
                               dt = new java.util.Date(val.getTime())
                        } else if (val instanceof java.util.Date){
                               dt = val
                        } else if (val == null){
                               return "";               
                        } else {
                                throw new RuntimeException("formatDate requires java.util.Date or java.sql.Date")
                        }

                        if (format == null){
                                return sdf_short.format(dt)
                        } else if (format.toString().equalsIgnoreCase("short")){
                                return sdf_short.format(dt)
                        } else if (format.toString().equalsIgnoreCase("long")){
                                return sdf_long.format(dt)
                        } else {
                                SimpleDateFormat sdf = new SimpleDateFormat(format.toString())
                                return sdf.format(dt);
                        }
                }
        }

        private static def addProperties(java.lang.Class clazz) {
                clazz.metaClass.static.addProperties = {app, propFile ->
                        def en = propFile.propertyNames()
                        while (en.hasMoreElements()) {
                                String key = en.nextElement()
                                app[key] = propFile.get(key)
                        }
                }
        }

        private static def addToJson(java.lang.Class clazz) {
                clazz.metaClass.static.toJson = {obj ->
                        return new Gson().toJson(obj)
                }
        }

        private static def addFromJson(java.lang.Class clazz) {
                clazz.metaClass.static.fromJson = {Object obj, Class cls->
                        return new Gson().fromJson(obj, cls)
                }

                clazz.metaClass.static.fromJson = {Object obj, Type t->
                        return new Gson().fromJson(obj, t)
                }
        }

        private static def addSendEmail(Class clazz) {
                clazz.metaClass.static.sendEmail = {Map prop ->
                        def to = prop.to
                        def bcc = prop.bcc
                        def cc = prop.cc

                        def from = prop.from
                        def subject = prop.subject
                        def body = prop.body
                        def htmlBody = prop.htmlBody
                        def attachments = prop.attachments

                        Email email = new Email()
                        email.setFrom(from)
                        email.subject = subject
                        email.body = body
                        email.htmlBody = htmlBody

                        if (from == null)
                                throw new SendEmailException("from address required");

                        if (attachments != null) {
                                if (attachments instanceof Map) {
                                        email.attachments = attachments
                                } else {
                                        throw new SendEmailException("Attachments must be in a map<FileName,Attachment>")
                                }
                        }

                        if (to == null && bcc == null && cc == null){
                                throw new SendEmailException("\"to\", \"cc\" or \"bcc\"  required to send an email")
                        }

                        if (to instanceof List) {
                                email.setRecipients(to)
                        } else if (to instanceof String) {
                                def l = []
                                l.addAll(Arrays.asList(to.toString().split(",")))
                                email.setRecipients(l)
                        }

                        if (cc instanceof List) {
                                email.setCc(cc)
                        } else if (cc instanceof String) {
                                def l = []
                                l.addAll(Arrays.asList(cc.toString().split(",")))
                                email.setCc(l)
                        }

                        if (bcc instanceof List) {
                                email.setBcc(bcc)
                        } else if (bcc  instanceof String) {
                                def l = []
                                l.addAll(Arrays.asList(bcc.toString().split(",")))
                                email.setBcc(l)
                        }

                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        email.host = app.getAttribute("smtp.host") ?: EasyGServer.propertiesFile.getString("smtp.host")
                        email.port = toInt(app.getAttribute("smtp.port") ?: EasyGServer.propertiesFile.getString("smtp.port"))
                        email.username = app.getAttribute("smtp.username") ?: EasyGServer.propertiesFile.getString("smtp.username")
                        email.password = app.getAttribute("smtp.password") ?: EasyGServer.propertiesFile.getString("smtp.password")
                        email.authenticationRequired = Boolean.parseBoolean(app.getAttribute("smtp.authentication.required") ?: EasyGServer.propertiesFile.getString("smtp.authentication.required"))
                        email.secure = Boolean.parseBoolean(app.getAttribute("smtp.secure") ?: EasyGServer.propertiesFile.getString("smtp.secure", "false"))
                        email.app = app

                        EmailService.addEmail(email);
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

        public static String htmlEncode(String s) {
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

        private static def addCopy(java.lang.Class clazz) {
                clazz.metaClass.static.copy = {src, dest ->
                                FileUtil.copy(src,dest)
                }
        }

        private static def addDelete(java.lang.Class clazz) {
                clazz.metaClass.static.delete = {String dir ->
                                FileUtil.deleteDirectory(dir)
                }
        }
}