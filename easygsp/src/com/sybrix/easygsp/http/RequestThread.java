package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;
import com.sybrix.easygsp.exception.ApplicationNotFoundException;
import com.sybrix.easygsp.exception.TemplateNotFoundException;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.logging.Logger;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import java.net.Socket;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;
import groovy.lang.MissingMethodException;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.CompilationFailedException;

import javax.servlet.ServletException;

/**
 * RequestThread <br/>
 * Description :
 */
public class RequestThread extends Thread {
        private static final Logger log = Logger.getLogger(RequestThread.class.getName());

        protected static String altExtension;
        protected static String groovyExtension;
        protected static String templateExtension;

        private Map<String, Application> applications;
        private Map<String, String> headers;
        private Socket socket;
        private long requestStartTime;
        private long requestEndTime;
        private long id = 0;
        private static int threadTimeoutInSeconds;

        private File root = null;

        private List<Language> languages = new ArrayList();
        private List<Charset> charsets = new ArrayList();

        // if thread still alive after this time it will be killed
        private long stopTime;
        private ResponseImpl response = null;

        private static String viewExtension;
        private static String defaultExtension;

        static {
                threadTimeoutInSeconds = EasyGServer.propertiesFile.getInt("thread.timeout");
                groovyExtension = EasyGServer.propertiesFile.getString("groovy.extension");
                altExtension = EasyGServer.propertiesFile.getString("alt.groovy.extension");
                templateExtension = EasyGServer.propertiesFile.getString("template.extension");
                viewExtension = EasyGServer.propertiesFile.getString("view.extension");
                defaultExtension = EasyGServer.propertiesFile.getString("default.welcome.page.extension");
        }

        public RequestThread(Socket socket, Map applications) {
                requestStartTime = System.currentTimeMillis();
                stopTime = requestStartTime + (threadTimeoutInSeconds * 1000);
                this.socket = socket;
                this.applications = applications;
        }

        public void run() {
                Application application = null;
                long startTime = System.currentTimeMillis();
                CustomServletBinding binding = null;

                try {
                        // bufferize the io stream
                        InputStream inputStream = initializeInputStream(socket);

                        // parse headers from SCGI formatted ios
                        headers = SCGIParser.parse(inputStream);

                        // do this now because, we may need to send an error
                        response = new ResponseImpl(socket.getOutputStream());

                        String appName = parseAppFolderName();
                        application = applications.get(appName);

                        // set the appId as soon as possible, even at the cost of having to repeat this line
                        ThreadAppIdentifier.set(application);

                        // if null, no app. Start app and then set it
                        if (application == null) {
                                application = EasyGServer.loadApplicationFromFileSystem(applications, appName, headers.get(RequestHeaders.DOCUMENT_ROOT));
                                ThreadAppIdentifier.set(application);
                        }

                        if (!application.isStarted()) {
                                log.log(FINE, "starting application: " + application.getAppPath());
                                application.startApplication();

                                // did someone modify the web.groovy file ?
                        } else if (application.groovyFileUpdated()) {
                                log.log(FINE, "restarting application: " + application.getAppPath());
                                application.killApp();
                                application = EasyGServer.loadApplicationFromFileSystem(applications, appName, headers.get(RequestHeaders.DOCUMENT_ROOT));
                                ThreadAppIdentifier.set(application);
                                application.startApplication();
                        }

                        RequestImpl request = new RequestImpl(inputStream, headers, application, response);
                        response.setHttpServletRequest(request);

                        request.setAttribute("_explicitForward", false);

                        SessionImpl session = (SessionImpl) request.getSession(false);
                        if (session != null)
                                session.updateLastAccessedTime();

                        binding = new CustomServletBinding(request, response, application, headers);
                        request.setServletBinding(binding);
                        //if (GSServer.propertiesFile.getString("logging.level").contains("FINE")) {
//                        for (Object o : headers.keySet()) {
//                                log.log(FINE, o + " = " + headers.get(o));
//                                System.out.println(o + " = " + headers.get(o));
//                        }

                        //determineBundle(headers);
                        //loadBundle(application, binding);

                        // build the script path
                        String scriptPath = null;
                        if (application.isVirtualHost())
                                scriptPath = headers.get(RequestHeaders.SCRIPT_NAME).substring(1);
                        else
                                scriptPath = headers.get(RequestHeaders.SCRIPT_NAME).substring(application.getAppName().length() + 2);

                        request.getServletBinding().setVariable("scriptPath", scriptPath);


                        // template stuff
                        if (File.separator.equals("\\")) {
                                root = new File(application.getAppPath() + File.separator + scriptPath.replaceAll("/", "\\\\"));
                        } else {
                                root = new File(application.getAppPath() + File.separator + scriptPath);
                        }

                        TemplateTL.get().setTemplateRoot(root.getParent());
                        RequestThreadInfo.get().setCurrentFile(root.getAbsolutePath());

                        //process the request
                        if (scriptPath.endsWith(templateExtension)) {
                                RequestThreadInfo.get().setTemplateRequest(true);
                                processTemplateRequest(scriptPath, application.getGroovyScriptEngine(), binding);
                        } else {
                                processScriptRequest(scriptPath, application.getGroovyScriptEngine(), binding);
                        }

                } catch (ApplicationNotFoundException e) {
                        sendError(404, response, e, null, binding);
                } catch (IOException e) {
                        log.log(SEVERE, "SCGIParsing failed", e);
                } catch (SecurityException e) {
                        log.log(SEVERE, e.getMessage(), e);
                } catch (Throwable e) {
                        if (e.toString().contains("ThreadDeath") && root != null) {
                                log.fine("Thread killed the hard way. script: " + root.getAbsolutePath());
                        } else
                                log.log(SEVERE, "Unknown throwable exception occurred. message: " + e.getMessage(), e);
                } finally {

                        // send the response to the client
                        response.sendResponse();

                        closeSocket(socket);
                        requestEndTime = System.currentTimeMillis();

                }

                log.fine("took " + (System.currentTimeMillis() - startTime) + " ms");
        }

        private void determineBundle(Map headers) {
                String charsetHeader = (String) headers.get(RequestHeaders.ACCEPT_CHARSET);
                String languageHeader = (String) headers.get(RequestHeaders.ACCEPT_LANGUAGE);
                String c[] = null;

                if (charsetHeader != null) {
                        c = charsetHeader.split(",");
                        for (String charset : c) {
                                charsets.add(new Charset(charset));
                        }
                }

                if (languageHeader != null) {
                        c = languageHeader.split(",");
                        for (String langauge : c) {
                                languages.add(new Language(langauge));
                                // add language without country code, this way we can just loop thru the list
                                if (langauge.indexOf('-') > 0)
                                        languages.add(new Language(langauge.split("-")[0], .005));
                        }
                }
                Collections.sort(languages);
                Collections.sort(charsets);
        }

        private void loadBundle(Application application, CustomServletBinding binding) {
                Language language = null;
                for (Language selectedLanguage : languages) {
                        if (application.getResourceBundles().containsKey(selectedLanguage.getLanguage())) {
                                language = selectedLanguage;
                                binding.setVariable("resource", application.getResourceBundles().get(selectedLanguage.getLanguage()));
                                break;
                        } else {

                                try {
                                        //File f = new File(application.getAppPath() + File.separator + "WEB-INF" + File.separator + "i18n" + File.separator + "resources_" + selectedLanguage.getLanguageCode().toLowerCase() + "_" + selectedLanguage.getCountryCode().toUpperCase() + ".properties");
                                        ResourceBundle bundle = ResourceBundle.getBundle("resources", selectedLanguage.getLocale(), new BundleClassLoader(application.getAppPath(), application.getGroovyScriptEngine().getGroovyClassLoader()));

                                        if (bundle != null) {

                                                // loop thru prop file, and add values to map
                                                Map map = new HashMap();
                                                Iterator i = bundle.keySet().iterator();
                                                while (i.hasNext()) {
                                                        String key = (String) i.next();
                                                        try {
                                                                map.put(key, bundle.getString(key));
                                                        } catch (Exception e) {
                                                                e.printStackTrace();
                                                        }
                                                }

                                                Map staticResourceBundle = Collections.unmodifiableMap(map);
                                                application.getResourceBundles().put(selectedLanguage.getLanguage(), staticResourceBundle);
                                                binding.setVariable("resource", staticResourceBundle);
                                                language = selectedLanguage;
                                                log.info("language=" + selectedLanguage.getLanguage());
                                                break;
                                        }
                                        //}
                                } catch (MissingResourceException e) {
                                        log.log(FINE, "Resource bundle not found for locale: " + selectedLanguage.getLocale(), e);
                                }

                        }
                }

                if (language != null) {
                        response.setLocale(language.getLocale());
                }
        }

        private void closeSocket(Socket socket) {
                try {
                        socket.close();
                } catch (IOException e) {

                }
        }

        private String parseAppFolderName() {
                String webAppDir = EasyGServer.propertiesFile.getString("groovy.webapp.dir");
                if (headers.get(RequestHeaders.SERVER_SOFTWARE).contains("Apache")) {
                        headers.put(RequestHeaders.SCRIPT_NAME, headers.get(RequestHeaders.SCRIPT_NAME) + headers.get(RequestHeaders.PATH_INFO));
                }

                String scriptName = headers.get(RequestHeaders.SCRIPT_NAME);
                if (!scriptName.endsWith(altExtension) && !scriptName.endsWith(groovyExtension) && !scriptName.endsWith(templateExtension)) {
                        if (headers.get(RequestHeaders.SCRIPT_NAME).endsWith("/")) {
                                headers.put(RequestHeaders.SCRIPT_NAME, headers.get(RequestHeaders.SCRIPT_NAME) + "index" + defaultExtension);
                        } else {
                                headers.put(RequestHeaders.SCRIPT_NAME, headers.get(RequestHeaders.SCRIPT_NAME) + "/index" + defaultExtension);
                        }
                }

                if ((EasyGServer.isWindows && headers.get(RequestHeaders.DOCUMENT_ROOT).equalsIgnoreCase(webAppDir)) || headers.get(RequestHeaders.DOCUMENT_ROOT).equals(webAppDir)) {
                        scriptName = headers.get(RequestHeaders.SCRIPT_NAME);
                        return scriptName.substring(1, scriptName.indexOf('/', 1));
                } else {
                        scriptName = headers.get(RequestHeaders.DOCUMENT_ROOT);
                        int index = scriptName.lastIndexOf('/', scriptName.length() - 2);

                        return scriptName.substring(index + 1, scriptName.length() - 1);
                }
        }

        private InputStream initializeInputStream(Socket socket) throws IOException {
                return new BufferedInputStream(socket.getInputStream());
        }

        public long getRequestEndTime() {
                return requestEndTime;
        }

        public void setRequestEndTime(long requestEndTime) {
                this.requestEndTime = requestEndTime;
        }

        public long getRequestStartTime() {
                return requestStartTime;
        }

        public void setRequestStartTime(long requestStartTime) {
                this.requestStartTime = requestStartTime;
        }

        public static void processScriptRequest(final String scriptPath, final GSE3 gse, final CustomServletBinding binding) {
                ResponseImpl response = (ResponseImpl) binding.getVariable("response");

                Application application = (Application) binding.getVariable("application");

                try {
                        Closure closure = new Closure(gse) {

                                public Object call() {
                                        try {
                                                RequestImpl request = (RequestImpl) binding.getVariable("request");
                                                ResponseImpl response = (ResponseImpl) binding.getVariable("response");

                                                ((GSE3) getDelegate()).run(scriptPath.replace(altExtension, groovyExtension), binding);

                                                Boolean alreadyForwarded = (Boolean) request.getAttribute("_explicitForward");

                                                if ((response.getBufferContentSize() == null) && alreadyForwarded == false) {
                                                        request.forwardToView(scriptPath.replace(altExtension, viewExtension));
//                                                      int i = scriptPath.lastIndexOf('/');
//                                                        if (i == -1)
//                                                                //replace .groovy w/ .gsp
//                                                                request.forward(scriptPath.replace(altExtension, viewExtension));
//                                                        else
//                                                                request.forward(scriptPath.substring(i + 1, scriptPath.length()).replace(altExtension, viewExtension));
                                                }

                                                return null;

                                        } catch (MissingPropertyException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        } catch (MissingMethodException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);

                                        } catch (ResourceException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);

                                        } catch (ScriptException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                if (e.getCause() instanceof FileNotFoundException) {
                                                        sendError(404, scriptPath, gse, binding, e);
                                                } else {
                                                        sendError(500, scriptPath, gse, binding, e);
                                                }
                                        } catch (IOException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);

                                        } catch (ServletException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        } catch (Throwable e) {
                                                log.log(Level.SEVERE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        }

                                        return null;
                                }

                        };
                        GroovyCategorySupport.use(CustomServletCategory.class, closure);

//                        gse.run(scriptPath, binding);
                        if (response.getStatusCode() == 0)
                                response.setStatus(200);
                } catch (MultipleCompilationErrorsException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        sendError(500, scriptPath, gse, binding, e);
                } catch (SecurityException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        sendError(500, scriptPath, gse, binding, e);
                } catch (TemplateNotFoundException e) {
                        log.log(Level.INFO, e.getMessage(), e);
                        sendError(500, scriptPath, gse, binding, e);
                } catch (Throwable e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        sendError(500, scriptPath, gse, binding, e);
                }
        }


        public static void processTemplateRequest(final String scriptPath, final GSE3 gse, final CustomServletBinding binding) {
                ResponseImpl response = (ResponseImpl) binding.getVariable("response");

                try {
                        Closure closure = new Closure(gse) {

                                public Object call() {
                                        try {
                                                RequestImpl request = (RequestImpl) binding.getVariable("request");
                                                ResponseImpl response = (ResponseImpl) binding.getVariable("response");
                                                Application application = (Application) binding.getVariable("application");

                                                application.getTemplateServlet().service(scriptPath, request, response, binding);
//
//                                                ((GSE3) getDelegate()).run(scriptPath.replace(altExtension, groovyExtension), binding);
//
//                                                Boolean alreadyForwarded = (Boolean) request.getAttribute("_explicitForward");
//                                                long bufferSize = response.getBufferContentSize() == null ? 0 : response.getBufferContentSize();
//                                                if (bufferSize == 0 && alreadyForwarded == false) {
//                                                        int i = scriptPath.lastIndexOf('/');
//                                                        if (i == -1)
//                                                                //replace .groovy w/ .gsp
//                                                                request.forward(scriptPath.replace(groovyExtension, altExtension));
//                                                        else
//                                                                request.forward(scriptPath.substring(i + 1, scriptPath.length()).replace(groovyExtension, altExtension));
//                                                }

                                                return null;

                                        } catch (MissingPropertyException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        } catch (MissingMethodException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);

//                                        } catch (ResourceException e) {
//                                                log.log(Level.SEVERE, e.getMessage(), e);
//                                                sendError(500, scriptPath, gse, binding, e);
//
//                                        } catch (ScriptException e) {
//                                                log.log(Level.INFO, e.getMessage(), e);
//                                                if (e.getCause() instanceof FileNotFoundException) {
//                                                        sendError(404, scriptPath, gse, binding, e);
//                                                } else {
//                                                        sendError(500, scriptPath, gse, binding, e);
//                                                }
                                        } catch (IOException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        } catch (CompilationFailedException e) {
                                                log.log(Level.SEVERE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        } catch (GroovyRuntimeException e) {
                                                log.log(Level.SEVERE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        } catch (ServletException e) {
                                                log.log(Level.SEVERE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        } catch (Throwable e) {
                                                log.log(Level.SEVERE, e.getMessage(), e);
                                                sendError(500, scriptPath, gse, binding, e);
                                        }

                                        return null;
                                }

                        };
                        GroovyCategorySupport.use(CustomServletCategory.class, closure);

//                        gse.run(scriptPath, binding);
                        if (response.getStatusCode() == 0)
                                response.setStatus(200);
                } catch (MultipleCompilationErrorsException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        sendError(500, scriptPath, gse, binding, e);
                } catch (SecurityException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        sendError(500, scriptPath, gse, binding, e);
                } catch (TemplateNotFoundException e) {
                        log.log(Level.INFO, e.getMessage(), e);
                        sendError(500, scriptPath, gse, binding, e);
                } catch (Throwable e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        sendError(500, scriptPath, gse, binding, e);
                }
        }

//        public static void processTemplateRequest(final String scriptPath, final GSE3 gse, final CustomServletBinding binding) {
//                ResponseImpl response = (ResponseImpl) binding.getVariable("response");
//
//                Application application = (Application) binding.getVariable("application");
//                final TemplateServlet templateServlet = application.getTemplateServlet();
//
//                try {
//                        Closure closure = new Closure(templateServlet) {
//
//                                public Object call() {
//                                        try {
//                                                RequestImpl request = (RequestImpl) binding.getVariable("request");
//                                                ResponseImpl response = (ResponseImpl) binding.getVariable("response");
//
//                                                templateServlet.service(scriptPath, request, response, binding);
//
//                                                return null;
//
//                                        } catch (MissingPropertyException e) {
//                                                log.log(Level.SEVERE, e.getMessage(), e);
//                                                sendError(500, scriptPath, gse, binding, e);
//                                        } catch (MissingMethodException e) {
//                                                log.log(Level.SEVERE, e.getMessage(), e);
//                                                sendError(500, scriptPath, gse, binding, e);
//                                        } catch (NullPointerException e) {
//                                                log.log(Level.SEVERE, e.getMessage(), e);
//                                                sendError(500, scriptPath, gse, binding, e);
//                                        } catch (IOException e) {
//                                                log.log(Level.SEVERE, e.getMessage(), e);
//                                                sendError(500, scriptPath, gse, binding, e);
//                                        } catch (Exception e) {
//                                                log.log(Level.SEVERE, e.getMessage(), e);
//                                                sendError(500, scriptPath, gse, binding, e);
//                                        }
//
//                                        return null;
//                                }
//
//                        };
//                        GroovyCategorySupport.use(CustomServletCategory.class, closure);
//
////                        gse.run(scriptPath, binding);
//                        if (response.getStatusCode() == 0)
//                                response.setStatus(200);
////                } catch (MultipleCompilationErrorsException e) {
////                        log.log(Level.SEVERE, e.getMessage(), e);
////                        sendError(500, scriptPath, gse, binding, e);
////                } catch (SecurityException e) {
////                        log.log(Level.SEVERE, e.getMessage(), e);
////                        sendError(500, scriptPath, gse, binding, e);
////                } catch (TemplateNotFoundException e) {
////                        log.log(Level.INFO, e.getMessage(), e);
////                        sendError(500, scriptPath, gse, binding, e);
//                } catch (Throwable e) {
//                       e.printStackTrace();
//                }
//        }

        private static void sendError(int errorCode, String scriptPath, GSE3 gse, CustomServletBinding binding, Throwable e) {
                ResponseImpl response = (ResponseImpl) binding.getVariable("response");
                Application application = (Application) binding.getVariable("application");
                RequestImpl request = (RequestImpl) binding.getVariable("request");
                StackTraceElement stackTraceElement = findErrorInStackTrace(binding, e);

                TemplateTL.get().setTemplateRoot(null);
                try {
                        //String content = getErrorFileContent(errorCode);

                        if (application.hasCustomErrorFile("error" + errorCode + groovyExtension)) {
                                response.setStatus(errorCode, e.getMessage());
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                e.printStackTrace(pw);

                                String errorMessage = sw.toString().replaceAll("\n", "<br/>");
                                request.setAttribute("script", scriptPath);
                                request.setAttribute("errorMessage", errorMessage);
                                request.setAttribute("error", e);
                                request.setAttribute("stackTraceElement", stackTraceElement);

                                response.setStatus(errorCode);

                                gse.run("errors/error" + errorCode + groovyExtension, binding);
                        } else {
                                sendError(errorCode, response, e, stackTraceElement, binding);
                        }
                        response.flushBuffer();

                } catch (Exception e1) {
                        log.log(Level.FINE, e1.getMessage(), e1);
                        sendError(errorCode, response, e, stackTraceElement, binding);
                }
        }

        private static StackTraceElement findErrorInStackTrace(CustomServletBinding binding, Throwable e) {
                File f = null;

                f = new File(RequestThreadInfo.get().getCurrentFile());

                if (f != null) {
                        if (e.getStackTrace() != null) {
                                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                                        if (stackTraceElement.getFileName() != null) {
                                                if (stackTraceElement.getFileName().startsWith(f.getName())) {
                                                        return stackTraceElement;
                                                }
                                        }
                                }
                        }
                }
                return null;
        }

        private static void sendError(int errorCode, ResponseImpl response, Throwable e, StackTraceElement stackTraceElement, CustomServletBinding binding) {
                TemplateTL.get().setTemplateRoot(null);
                try {

                        String content = getErrorFileContent(errorCode);
                        response.setStatus(errorCode, e.getMessage());

                        String uniqueScriptName = RequestThreadInfo.get().getUniqueScriptName();
                        String realScriptName = RequestThreadInfo.get().getRealScriptName();

                        StringWriter sw = null;
                        if (errorCode >= 500) {

                                sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);

                                if (stackTraceElement != null)
                                        pw.write("Error occurred in " + stackTraceElement.getFileName() + " @ lineNumber: " + (stackTraceElement.getLineNumber() - 2) + "\n\n");

                                e.printStackTrace(pw);
                                response.getWriter().flush();

                                if (uniqueScriptName != null && realScriptName != null) {
                                        if (response.getBufferContentSize() == 0)
                                                content = content.replace("??", sw.toString().replaceAll("\n", "<br/>").replaceAll(uniqueScriptName, realScriptName));
                                        else
                                                content = "<div>" + sw.toString().replaceAll("\n", "<br/>").replaceAll(uniqueScriptName, realScriptName) + "</div>";
                                } else {
                                        if (response.getBufferContentSize() == 0)
                                                content = content.replace("??", sw.toString().replaceAll("\n", "<br/>"));
                                        else
                                                content = "<div>" + sw.toString().replaceAll("\n", "<br/>") + "</div>";
                                }
                        }

                        response.getWriter().println(content);

                } catch (Exception e1) {
                        log.log(Level.SEVERE, "Error occurred sending static error file", e1);
                }
        }

        public void sendError(String message) {
                TemplateTL.get().setTemplateRoot(null);
                try {
                        String content = getErrorFileContent(500);
                        response.setStatus(500, "");
                        response.getWriter().print(message);

                } catch (Exception e1) {
                        log.log(SEVERE, e1.getMessage(), e1);
                }
        }

        private static String getErrorFileContent(int errorCode) {
                StringWriter sw = null;
                try {
                        File errorFile = new File(EasyGServer.APP_DIR + File.separator + "conf" + File.separator + "errors" + File.separator + errorCode + ".html");
                        FileReader fr = new FileReader(errorFile);
                        sw = new StringWriter((int) errorFile.length());

                        char ch[] = new char[2048];
                        int c = 0;
                        while ((c = fr.read(ch, 0, ch.length)) != -1) {
                                sw.write(ch, 0, c);
                        }
                        return sw.toString();
                } catch (Exception e) {
                        e.printStackTrace();
                }

                return "";
        }

        public long getStopTime() {
                return stopTime;
        }

        //        public void service(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, GroovyScriptEngine gse, final String scriptUri) throws IOException {
        //
        //
        //                // Set it to HTML by default
        //                response.setContentType("text/html; charset=UTF-8");
        //
        //                // Set up the script context
        //                final Binding binding = new groovy.servlet.ServletBinding(request, response, servletContext);
        //
        //                // Run the script
        //                try {
        //                        Closure closure = new Closure(gse) {
        //
        //                                public Object call() {
        //                                        try {
        //                                                return ((GroovyScriptEngine) getDelegate()).run(scriptUri, binding);
        //                                        } catch (ResourceException e) {
        //                                                throw new RuntimeException(e);
        //                                        } catch (ScriptException e) {
        //                                                throw new RuntimeException(e);
        //                                        }
        //                                }
        //
        //                        };
        //                        GroovyCategorySupport.use(ServletCategory.class, closure);
        //                        /*
        //                        * Set reponse code 200.
        //                        */
        //                        response.setStatus(HttpServletResponse.SC_OK);
        //                } catch (RuntimeException runtimeException) {
        //                        StringBuffer error = new StringBuffer("GroovyServlet Error: ");
        //                        error.append(" script: '");
        //                        error.append(scriptUri);
        //                        error.append("': ");
        //                        Throwable e = runtimeException.getCause();
        //                        /*
        //                        * Null cause?!
        //                        */
        //                        if (e == null) {
        //                                error.append(" Script processfing failed.");
        //                                error.append(runtimeException.getMessage());
        //                                if (runtimeException.getStackTrace().length > 0)
        //                                        error.append(runtimeException.getStackTrace()[0].toString());
        //                                servletContext.log(error.toString());
        //                               //System.err.println(error.toString());
        //                                runtimeException.printStackTrace(System.err);
        //                                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.toString());
        //                                return;
        //                        }
        //                        /*
        //                        * Resource not found.
        //                        */
        //                        if (e instanceof ResourceException) {
        //                                error.append(" Script not found, sending 404.");
        //                                servletContext.log(error.toString());
        //                                System.err.println(error.toString());
        //                                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        //                                return;
        //                        }
        //                        /*
        //                        * Other internal error. Perhaps syntax?!
        //                        */
        //                        servletContext.log("An error occurred processing the request", runtimeException);
        //                        error.append(e.getMessage());
        //                        if (e.getStackTrace().length > 0)
        //                                error.append(e.getStackTrace()[0].toString());
        //                        servletContext.log(e.toString());
        //                        System.err.println(e.toString());
        //                        runtimeException.printStackTrace(System.err);
        //                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
        //                } finally {
        //                        /*
        //                        * Finally, flush the response buffer.
        //                        */
        //                        response.flushBuffer();
        //                        // servletContext.log("Flushed response buffer.");
        //                }
        //        }


}
