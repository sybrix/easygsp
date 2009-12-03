package com.sybrix.easygsp.http

import com.sybrix.easygsp.server.EasyGServer


public class StaticControllerMethods {
        public static addMethods(Class clazz) {
                addLogMethod(clazz)
                addLogThrowableMethod(clazz)
                addLogThrowableAndMessageMethod(clazz)
                addConsoleWriteMethod(clazz)
                addUrlDencode(clazz)
                addUrlEncode(clazz)
                addHtmlEncode(clazz)
                // prop file
                // email
                // cookie
                // dbconnection
        }

        private static def addLogMethod(java.lang.Class clazz) {
                clazz.metaClass.'static'.log = {String logMessage ->
                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        LogMessage lm = new LogMessage(logMessage, app)
                        if (app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console", false)){
                                System.out.println lm.toString()
                        }

                        EasyGSPLogger.getInstance().log(lm)
                }
        }

        private static def addLogThrowableAndMessageMethod(java.lang.Class clazz) {
                clazz.metaClass.'static'.log = {String logMessage, Throwable t ->
                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        LogMessage lm = new LogMessage(logMessage, t, app)
                        if (app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console", false)){
                                System.out.println lm.toString()
                        }

                        EasyGSPLogger.getInstance().log(lm)
                }
        }


        private static def addLogThrowableMethod(java.lang.Class clazz) {
                clazz.metaClass.'static'.log = {Throwable t ->
                        ServletContextImpl app = RequestThreadInfo.get().getApplication()
                        LogMessage logMessage = new LogMessage(t, app)
                        if (app.getAttribute("logToConsole") == true && EasyGServer.propertiesFile.getBoolean("log.to.console", false)){
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

        
}