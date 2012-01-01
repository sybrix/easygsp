package com.sybrix.easygsp.http;

import com.sybrix.easygsp.db.CurrentSQLInstance;
import com.sybrix.easygsp.db.Model;
import com.sybrix.easygsp.email.Email;
import com.sybrix.easygsp.email.EmailService;
import com.sybrix.easygsp.exception.SendEmailException;
import com.sybrix.easygsp.logging.EasyGSPLogger;
import com.sybrix.easygsp.logging.LogMessage;
import com.sybrix.easygsp.logging.LoggingLevel;
import com.sybrix.easygsp.server.EasyGServer;
import com.sybrix.easygsp.util.*;
import groovy.lang.Closure;
import groovy.sql.Sql;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.groovy.runtime.GStringImpl;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author David Lee
 */
public class StaticMethods {
        static SimpleDateFormat sdf_short = new SimpleDateFormat("MM/dd/yyyy");
        static SimpleDateFormat sdf_long = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

        public static Timestamp getNow() {
                return new Timestamp(System.currentTimeMillis());
        }

        public static PropertiesFile loadPropertiesFile(String fileName) {
                ServletContextImpl app = RequestThreadInfo.get().getApplication();
                String filePath = fileName;
                if (fileName.indexOf('/') < 0 && fileName.indexOf('\\') < 0) {
                        filePath = app.getAppPath() + File.separator + "WEB-INF" + File.separator + fileName;
                }

                return new PropertiesFile(filePath);
        }

        public static void log(String logMessage) {
                ServletContextImpl app = RequestThreadInfo.get().getApplication();
                LogMessage lm = new LogMessage(logMessage, app);
                logToConsole(app, lm);

                EasyGSPLogger.getInstance().logMessage(lm);
        }

        public static void log(Throwable t) {
                ServletContextImpl app = RequestThreadInfo.get().getApplication();
                LogMessage lm = new LogMessage(t, app);
                logToConsole(app, lm);
                EasyGSPLogger.getInstance().logMessage(lm);

        }

        public static void log(String logMessage, Throwable t) {
                ServletContextImpl app = RequestThreadInfo.get().getApplication();
                LogMessage lm = new LogMessage(logMessage, t, app);
                logToConsole(app, lm);

                EasyGSPLogger.getInstance().logMessage(lm);
        }

        public static void log(LoggingLevel level, String logMessage) {
                ServletContextImpl app = RequestThreadInfo.get().getApplication();
                LogMessage lm = new LogMessage(level, logMessage, app);
                logToConsole(app, lm);

                EasyGSPLogger.getInstance().logMessage(lm);
        }

        private static void logToConsole(ServletContextImpl app, LogMessage lm) {
                if ((Boolean) app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console",
                        false) && (app.getLoggingLevel().ordinal() <= lm.getLevel().ordinal())) {
                        System.out.print(lm.toString());
                }
        }

        public static void log(LoggingLevel level, Throwable t) {
                ServletContextImpl app = RequestThreadInfo.get().getApplication();
                LogMessage logMessage = new LogMessage(level, t, app);
                logToConsole(app, logMessage);
                EasyGSPLogger.getInstance().logMessage(logMessage);

        }

        public static void log(LoggingLevel level, String logMessage, Throwable t) {
                ServletContextImpl app = RequestThreadInfo.get().getApplication();
                LogMessage lm = new LogMessage(level, logMessage, t, app);
                logToConsole(app, lm);

                EasyGSPLogger.getInstance().logMessage(lm);
        }

        public static void console(Object s) {
                System.out.println(s.toString());
        }

        public static String decode(String s) throws UnsupportedEncodingException {
                if (s == null)
                        return "";
                return java.net.URLDecoder.decode(s, "UTF-8");
        }

        public static String encode(String s) throws UnsupportedEncodingException {
                if (s == null)
                        return "";
                return java.net.URLEncoder.encode(s, "UTF-8");
        }

        public static Sql newSqlInstance() {
                return newSqlInstance(null);
        }


        public static Sql newSqlInstance(String dataSourceName) {
                Object ds = null;

                try {

                        if (dataSourceName == null) {
                                Sql db = CurrentSQLInstance.get();
                                if (db != null)
                                        return db;
                        }

                        ServletContextImpl app = RequestThreadInfo.get().getApplication();
                        if (dataSourceName == null)
                                dataSourceName = "";
                        else
                                dataSourceName += ".";


                        ds = app.getAttribute("__dataSource_" + dataSourceName);
                        String dataSourceClass = (String) app.getAttribute(dataSourceName + "datasource.class");


                        if (ds != null && dataSourceClass != null) {
                                return new Sql((DataSource) ds);
                        }
                        if (ds == null && dataSourceClass != null) {

                                String url = (String) app.getAttribute(dataSourceName + "datasource.url");
                                String pwd = (String) app.getAttribute(dataSourceName + "datasource.password");
                                String username = (String) app.getAttribute(dataSourceName + "datasource.username");

                                Class dsClass = Class.forName(dataSourceClass);

                                ds = dsClass.newInstance();
                                Map<String, String> dataSourceProperties = getDataSourceProperties(app, dataSourceName);
                                for (String property : dataSourceProperties.keySet()) {
                                        callMethod(ds, "set" + StringUtil.capFirstLetter(property), app.getAttribute(dataSourceProperties.get(property)));
                                }
//                                callMethod(ds, "setUserName", username);
//                                callMethod(ds, "setPassword", pwd);
//                                callMethod(ds, "setDatabase", url);
//                                callMethod(ds, "setMaxIdleTime", 30);
//                                callMethod(ds, "setPooling", true);
//                                callMethod(ds, "setMinPoolSize", 5);
//                                callMethod(ds, "setMaxPoolSize", 30);
//                                callMethod(ds, "setLoginTimeout", 10);

                                app.setAttribute("__dataSource_" + dataSourceName, ds);
                                return new Sql((DataSource) ds);

                        } else if (ds == null && dataSourceClass == null) {

                                String driver = (String) app.getAttribute(dataSourceName + "database.driver");
                                String url = (String) app.getAttribute(dataSourceName + "database.url");
                                String pwd = (String) app.getAttribute(dataSourceName + "database.password");
                                String username = (String) app.getAttribute(dataSourceName + "database.username");

                                return Sql.newInstance(url, username, pwd, driver);

                        }


                } catch (Exception e) {
                        throw new RuntimeException("newSqlInstance() failed. Make sure app['database.*]' properties are set and correct." + e.getMessage(), e);
                }

                return null;
        }

        private static Map<String, String> getDataSourceProperties(ServletContextImpl app, String datasourceName) {
                Map dataSourceProperties = new HashMap();
                Map attributes = app.getAttributes();
                for (Object key : attributes.keySet()) {
                        //Object val = attributes.get(key);
                        if (key.toString().startsWith(datasourceName + "datasource.")) {
                                if (!key.equals(datasourceName + "datasource.class")) {
                                        dataSourceProperties.put(key.toString().substring(key.toString().lastIndexOf(".") + 1), key);
                                }
                        }
                }

                return dataSourceProperties;

        }

        private static void callMethod(Object ds, String methodName, Object parameterValue) {

                try {
                        Method method = null;

                        Method[] methods = ds.getClass().getMethods();
                        for (Method m : methods) {
                                if (m.getName().equals(methodName) && m.getParameterTypes().length == 1) {
                                        method = m;
                                }
                        }

                        Class cls = method.getParameterTypes()[0];
                        if (cls.getName().contains("boolean")) {
                                cls = Boolean.class;
                        } else if (cls.getName().contains("int")) {
                                cls = Integer.class;
                        } else if (cls.getName().contains("long")) {
                                cls = Long.class;
                        } else if (cls.getName().contains("double")) {
                                cls = Double.class;
                        }

                        Constructor constructor = cls.getConstructor(String.class);
                        Object val = constructor.newInstance(parameterValue.toString());

                        Method m = ds.getClass().getMethod(methodName, method.getParameterTypes()[0]);
                        m.invoke(ds, val);
                } catch (Throwable e) {
                        throw new RuntimeException("Error setting DataSource property. datasource=" + ds.toString() + ", methodName=" + methodName + ", " +
                                "value=" + parameterValue, e);
                }
        }

//        private static void callMethod(Object ds, String method, String parameterValue) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//                Method m = ds.getClass().getMethod(method, String.class);
//                m.invoke(ds, parameterValue);
//        }
//
//        private static void callMethod(Object ds, String method, int parameterValue) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//                Method m = ds.getClass().getMethod(method, int.class);
//                m.invoke(ds, parameterValue);
//        }
//
//        private static void callMethod(Object ds, String method, boolean parameterValue) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//                Method m = ds.getClass().getMethod(method, boolean.class);
//                m.invoke(ds, parameterValue);
//        }

        public static String htmlEncode(String s) {
                StringBuffer encodedString = new StringBuffer("");
                char[] chars = s.toCharArray();
                for (char c : chars) {
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

        public static Double toDbl(Object val, Double defaultVal) {
                if (val == null)
                        return defaultVal;
                try {
                        return toDbl(val);
                } catch (Exception e) {
                        return defaultVal;
                }

        }

        public static Double toDbl(Object val) {
                if (val == null || isEmpty(val.toString()))
                        return null;

                return Double.parseDouble(val.toString());

        }

        public static BigDecimal toBD(Object val) {
                if (val == null || isEmpty(val.toString()))
                        return null;

                return new BigDecimal(val.toString());

        }

        public static BigDecimal toBD(Object val, BigDecimal defaultVal) {
                if (val == null)
                        return defaultVal;

                try {
                        return toBD(val);
                } catch (Exception e) {
                        return defaultVal;
                }
        }


        public static Integer toInt(Object val, Integer defaultVal) {
                if (val == null)
                        return defaultVal;

                try {
                        return toInt(val);
                } catch (Exception e) {
                        return defaultVal;
                }
        }

        public static Integer toInt(Object val) {
                if (val == null || isEmpty(val.toString()))
                        return null;
                return Integer.parseInt(val.toString());
        }

        public static Long toLong(Object val, Long defaultValue) {
                if (val == null)
                        return defaultValue;

                try {
                        return toLong(val);
                } catch (Exception e) {
                        return defaultValue;
                }
        }

        public static Long toLong(Object val) {

                if (val == null || isEmpty(val.toString()))
                        return null;

                if (val instanceof Number)
                        return ((Number) val).longValue();
                else
                        return Long.parseLong(val.toString());
        }

        public static Boolean isEmailValid(Object val) {
                if (val == null) {
                        return false;
                }
                return Validator.isEmailValid(val.toString());
        }

        public static Boolean isAlphaNumeric(Object val) {
                if (val == null) {
                        return false;
                }
                return Validator.isAlphaNumeric(val.toString());
        }

        public static Boolean isZipCode(Object val) {
                if (val == null) {
                        return false;
                }
                return Validator.isZipCodeValid(val.toString());
        }

        public static Boolean isPhone(Object val) {
                if (val == null) {
                        return false;
                }
                return Validator.isValidPhone(val.toString());
        }

        public static Boolean isNumeric(Object val) {
                if (val == null) {
                        return false;
                }
                return Validator.isNumeric(val.toString());
        }

        public static Boolean isEmpty(Object val) {
                if (val == null) {
                        return true;
                }
                return Validator.isEmpty(val);
        }

        public static Boolean isCreditCardValid(String val) {
                if (val == null) {
                        return false;
                }
                return Validator.isCreditCardValid(val);
        }

        public static Object ifNull(Object val, Object defaultVal) {
                if (Validator.isEmpty(val))
                        return defaultVal;
                else
                        return val;
        }

        public static String MD5(String val) {
                return Hash.MD5(val);
        }

        public static String SHA1(String val) {
                return Hash.SHA1(val);
        }

//        public static String format(Number val, String pattern) {
//                DecimalFormat decimalFormat = new DecimalFormat(pattern);
//                return decimalFormat.format(val.doubleValue());
//        }

        public static String format(Number val, String pattern) {
                DecimalFormat decimalFormat = new DecimalFormat(pattern);
                return decimalFormat.format(val.doubleValue());

        }

        public static String formatMoney(Number val) {
                return formatMoney(val, Locale.getDefault());
        }

        public static String formatMoney(Number val, Locale locale) {
                if (val == null)
                        return "";

                NumberFormat moneyFormatter = DecimalFormat.getCurrencyInstance(locale);

                if (val instanceof Double)
                        return moneyFormatter.format(val);
                else
                        return moneyFormatter.format(new Double(val.toString()));
        }

        public static String formatDate(java.util.Date val, String format) {

                Date dt;
                if (val instanceof java.util.Date) {
                        dt = new java.util.Date(((Date) val).getTime());
//                } else if (val instanceof java.util.Date) {
//                        dt = (Date) val;
                } else if (val == null) {
                        return "";
                } else {
                        throw new RuntimeException("formatDate requires java.util.Date or java.sql.Date");
                }

                if (format == null) {
                        return sdf_short.format(dt);
                } else if (format.toString().equalsIgnoreCase("short")) {
                        return sdf_short.format(dt);
                } else if (format.toString().equalsIgnoreCase("long")) {
                        return sdf_long.format(dt);
                } else {
                        SimpleDateFormat sdf = new SimpleDateFormat(format.toString());
                        return sdf.format(dt);
                }
        }

        public static void addProperties(ServletContextImpl app, PropertiesFile propFile) {
                Enumeration en = propFile.propertyNames();
                while (en.hasMoreElements()) {
                        String key = (String) en.nextElement();
                        app.setAttribute(key, propFile.get(key));
                }
        }

        public static void copy(String src, String dest) throws IOException {
                FileUtil.copy(src, dest);

        }

        public static Boolean delete(String dir) throws IOException {
                return FileUtil.deleteDirectory(dir);
        }


        public static void sendEmail(Map prop) {
                Object to = prop.get("to");
                Object bcc = prop.get("bcc");
                Object cc = prop.get("cc");

                String from = (prop.get("from") instanceof GStringImpl) ? prop.toString(): (String) prop.get("from");
                String subject = (prop.get("from") instanceof GStringImpl) ? prop.toString(): (String) prop.get("subject");
                String body = (prop.get("from") instanceof GStringImpl) ? prop.toString(): (String) prop.get("body");
                String htmlBody = (prop.get("from") instanceof GStringImpl) ? prop.toString(): (String) prop.get("htmlBody");

                Object attachments = prop.get("attachments");

                Email email = new Email();
                email.setFrom(from);
                email.setSubject(subject);
                email.setBody(body);
                email.setHtmlBody(htmlBody);

                if (from == null)
                        throw new SendEmailException("from address required");

                if (attachments != null) {
                        if (attachments instanceof Map) {
                                email.setAttachments((Map) attachments);
                        } else {
                                throw new SendEmailException("Attachments must be in a map<FileName,Attachment>");
                        }
                }

                if (to == null && bcc == null && cc == null) {
                        throw new SendEmailException("\"to\", \"cc\" or \"bcc\"  required to send an email");
                }

                if (to instanceof List) {
                        email.setRecipients((List) to);
                } else if (to instanceof String) {
                        List l = new ArrayList();
                        l.addAll(Arrays.asList(to.toString().split(",")));
                        email.setRecipients(l);
                }

                if (cc instanceof List) {
                        email.setCc((List) cc);
                } else if (cc instanceof String) {
                        List l = new ArrayList();
                        l.addAll(Arrays.asList(cc.toString().split(",")));
                        email.setCc(l);
                }

                if (bcc instanceof List) {
                        email.setBcc((List) bcc);
                } else if (bcc instanceof String) {
                        List l = new ArrayList();
                        l.addAll(Arrays.asList(bcc.toString().split(",")));
                        email.setBcc(l);
                }

                ServletContextImpl app = RequestThreadInfo.get().getApplication();
                email.setHost(app.getAttribute("smtp.host") == null ? EasyGServer.propertiesFile.getString("smtp.host") : (String) app.getAttribute("smtp.host"));
                email.setPort(app.getAttribute("smtp.port") == null ? Integer.valueOf(EasyGServer.propertiesFile.getString("smtp.port")) : Integer.valueOf((String) app.getAttribute("smtp.port")));

                email.setUsername(app.getAttribute("smtp.username") == null ? EasyGServer.propertiesFile.getString("smtp.username") : app.getAttribute("smtp.username").toString());
                email.setPassword(app.getAttribute("smtp.password") == null ? EasyGServer.propertiesFile.getString("smtp.password") : app.getAttribute("smtp.password").toString());
                email.setAuthenticationRequired(Boolean.parseBoolean(app.getAttribute("smtp.authentication.required") == null ? EasyGServer.propertiesFile.getString("smtp.authentication.required") : app.getAttribute("smtp.authentication.required").toString()));
                email.setSecure(Boolean.parseBoolean(app.getAttribute("smtp.secure") == null ? EasyGServer.propertiesFile.getString("smtp.secure", "false") : app.getAttribute("smtp.secure").toString()));
                email.setApp(app);

                EmailService.addEmail(email);
        }

        public static void logAppThrowableAndMessageMethod(ServletContextImpl app, String logMessage, Throwable t) {
                LogMessage lm = new LogMessage(logMessage, t, app);
                if ((Boolean) app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console", false)) {
                        System.out.println(lm.toString());
                }

                EasyGSPLogger.getInstance().logMessage(lm);
        }

        public static Date parseDate(Object dt) throws ParseException {
                if (isEmpty(dt.toString()))
                        return null;

                return sdf_short.parse(dt.toString());
        }

        public static Date parseDate(Object dt, String format) throws ParseException {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                return sdf.parse(dt.toString());
        }

        public static Object withTransaction(Closure closure) {
                return Model.withTransaction(closure);
        }

        public static byte[] decodeBase64(String data) {
                return Base64.decodeBase64(data.getBytes());
        }

        public static byte[] decodeBase64(byte[] data) {
                return Base64.decodeBase64(data);
        }

//        public static BigDecimal round(Object obj, int scale) {
//                BigDecimal d = new BigDecimal(obj.toString());
//                return d.setScale(scale, BigDecimal.ROUND_HALF_UP);
//        }
}
