package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;
import com.sybrix.easygsp.exception.ApplicationNotFoundException;
import com.sybrix.easygsp.exception.TemplateNotFoundException;

import java.io.*;
import java.util.*;
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

        private Map<String, ServletContextImpl> applications;
        private Map<String, String> headers;
        private Socket socket;
        private long requestStartTime;
        private long requestEndTime;
        private static long id = 0;
        private static int threadTimeoutInSeconds;

        private File root = null;

        private List<Language> languages = new ArrayList();
        private List<Charset> charsets = new ArrayList();

        // if thread still alive after this time it will be killed
        private long stopTime;
        private ResponseImpl response = null;

        private static String viewExtension;
        protected static String defaultExtension;
        private CustomServletBinding binding;
        private String scriptPath;
        private ServletContextImpl application;

        static {
                threadTimeoutInSeconds = EasyGServer.propertiesFile.getInt("thread.timeout", 30);
                groovyExtension = EasyGServer.propertiesFile.getString("groovy.extension");
                altExtension = EasyGServer.propertiesFile.getString("alt.groovy.extension");
                templateExtension = EasyGServer.propertiesFile.getString("template.extension");
                viewExtension = EasyGServer.propertiesFile.getString("view.extension");
                defaultExtension = EasyGServer.propertiesFile.getString("default.welcome.page.extension");
        }

        public RequestThread(Socket socket, Map applications) {
                super("EasyGSP Request Thread " + ++id);
                requestStartTime = System.currentTimeMillis();
                stopTime = requestStartTime + (threadTimeoutInSeconds * 1000);
                this.socket = socket;
                this.applications = applications;
        }

        public void run() {

                long startTime = System.currentTimeMillis();
                SessionImpl session = null;

                try {
                        // bufferize the io stream
                        InputStream inputStream = initializeInputStream(socket);

                        // parse headers from SCGI formatted ios
                        headers = SCGIParser.parse(inputStream);

                        // do this now because, we may need to send an error
                        response = new ResponseImpl(socket.getOutputStream());

                        ParsedRequest parsedRequest = parseAppFolderName();
                        application = applications.get(parsedRequest.getAppName());

                        // set the appId as soon as possible, even at the cost of having to repeat this line
                        RequestThreadInfo.get().setApplication(application);

                        // if null, no app. Start app and then set it
                        if (application == null) {
                                application = EasyGServer.loadApplicationFromFileSystem(applications, parsedRequest.getAppName(), parsedRequest.getAppPath());
                                RequestThreadInfo.get().setApplication(application);
                        }

                        RequestThreadInfo.get().setParsedRequest(parsedRequest);

                        if (!application.isStarted()) {
                                log.log(FINE, "starting application: " + application.getAppPath());
                                application.startApplication();
                                EasyGServer.sendToChannel(new ClusterMessage(application.getAppName(), application.getAppPath(), "", "appStart", new Object[]{}));
                                // did someone modify the web.groovy file ?

                        } else if (application.webGroovyUpdated()) {
                                log.log(FINE, "restarting application: " + application.getAppPath());

                                application = EasyGServer.loadApplicationFromFileSystem(applications, parsedRequest.getAppName(), parsedRequest.getAppPath());
                                RequestThreadInfo.get().setApplication(application);
                                application.startApplication();
                                EasyGServer.sendToChannel(new ClusterMessage(application.getAppName(), application.getAppPath(), "", "appStart", new Object[]{}));

                        }

                        RequestImpl request = new RequestImpl(inputStream, headers, application, response);
                        response.setHttpServletRequest(request);

                        // when false, request will auto forward to view
                        request.setAttribute("_explicitForward", false);

                        session = (SessionImpl) request.getSession(false);

                        binding = new CustomServletBinding(request, response, application, headers);

                        if (session != null){
                                session.updateLastAccessedTime();
                                expireFlashMessages(session);
                                binding.setVariable("flash", session.getFlash());
                        }

                        request.setServletBinding(binding);
                        RequestThreadInfo.get().setBinding(binding);

                        if (EasyGServer.propertiesFile.getString("logging.level", "SEVERE").equals("FINEST")) {
                                StringBuffer s = new StringBuffer();
                                s.append("SCGI headers\r\n");
                                for (Object o : headers.keySet()) {
                                        s.append("\t").append(o).append(" = ").append(headers.get(o)).append("\r\n");
                                }
                                log.log(FINEST, s.toString());
                        }

                        //determineBundle(headers);
                        //loadBundle(application, binding);

                        // build the script path
                        scriptPath = parsedRequest.getRequestFilePath();
//                        if (application.isVirtualHost())
//                                scriptPath = headers.get(RequestHeaders.SCRIPT_NAME).substring(1);
//                        else
//                                scriptPath = headers.get(RequestHeaders.SCRIPT_NAME).substring(application.getAppName().length() + 2);
//
//                        request.getServletBinding().setVariable("scriptPath", scriptPath);


                        // template stuff
                        //if (File.separator.equals("\\")) {
                        root = new File(scriptPath);
//                        } else {
//                                root = new File(application.getAppPath() + File.separator + scriptPath);
//                        }

                        RequestThreadInfo.get().getTemplateInfo().setTemplateRoot(root.getParent());
                        RequestThreadInfo.get().setCurrentFile(root.getAbsolutePath());

                        if (request.isMulitpart) {
                                request.parseFileUploads();
                                request.setAttribute("isMultipart", true);
                        }

//                        if (application.hasOnScriptStartMethod()) {
//                                try {
//                                        application.invokeWebMethod("onScriptStart", request, response);
//                                } catch (Exception e) {
//                                        application.setHasOnScriptStart(false);
//                                        e.printStackTrace();
//                                }
//                        }
                        //process the request
                        if (parsedRequest.getRequestURI().endsWith(templateExtension)) {
                                processTemplateRequest(parsedRequest.getRequestURI(), application.getGroovyScriptEngine(), binding);
                        } else {
                                processScriptRequest(parsedRequest.getRequestURI(), application.getGroovyScriptEngine(), binding);
                        }


                } catch (ApplicationNotFoundException e) {
                        sendError(404, scriptPath, application.getGroovyScriptEngine(), binding, e);
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

                if (session != null)
                        EasyGServer.sendToChannel(new ClusterMessage(application.getAppName(), application.getAppPath(), session.getId(), "session", new Object[]{session}));

                log.fine("took " + (System.currentTimeMillis() - startTime) + " ms");
        }

        private void expireFlashMessages(SessionImpl session) {
                Map<String, FlashMessage> flash = session.getFlash();
                Iterator i = flash.keySet().iterator();
                while(i.hasNext()){
                       String key = (String)i.next();
                       FlashMessage msg = flash.get(key);
                        if (msg.isExpired())
                                flash.remove(key);
                        else
                                msg.setExpired(true);
                }
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

        private void loadBundle(ServletContextImpl application, CustomServletBinding binding) {
                Language language = null;
                for (Language selectedLanguage : languages) {
                        if (application.getResourceBundles().containsKey(selectedLanguage.getLanguage())) {
                                language = selectedLanguage;
                                binding.setVariable("resource", application.getResourceBundles().get(selectedLanguage.getLanguage()));
                                break;
                        } else {

                                try {
                                        //File f = new File(application.getAppPath() + File.separator + "WEB-INF" + File.separator + "i18n" + File.separator + "resources_" + selectedLanguage.getLanguageCode().toLowerCase() + "_" + selectedLanguage.getCountryCode().toUpperCase() + ".properties");
                                        ResourceBundle bundle = ResourceBundle.getBundle("resources", selectedLanguage.getLocale());

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

        protected void closeSocket() {
                closeSocket(socket);
        }

        private void closeSocket(Socket socket) {
                try {
                        socket.close();
                } catch (IOException e) {

                }
        }

        private ParsedRequest parseAppFolderName() {

                if (headers.get(RequestHeaders.SERVER_SOFTWARE).contains("Apache")) {
                        return Parsers.ApacheParser.parseRequestFromHeader(headers);
                } else {
                        return Parsers.LightyParser.parseRequestFromHeader(headers);
                }


                //if ((EasyGServer.isWindows && headers.get(RequestHeaders.DOCUMENT_ROOT).equalsIgnoreCase(webAppDir)) || headers.get(RequestHeaders.DOCUMENT_ROOT).equals(webAppDir)) {
                //scriptName = headers.get(RequestHeaders.SCRIPT_NAME);
//                        return scriptName.substring(1, scriptName.indexOf('/', 1));
//                } else {
//                        scriptName = headers.get(RequestHeaders.DOCUMENT_ROOT);
//                        int index = scriptName.lastIndexOf('/', scriptName.length() - 2);
//
//                        return scriptName.substring(index + 1, scriptName.length() - 1);
//                }
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

        public static void processScriptRequest(final String requestURI, final GSE4 gse, final CustomServletBinding binding) {
                final ResponseImpl response = (ResponseImpl) binding.getVariable("response");

                ServletContextImpl application = (ServletContextImpl) binding.getVariable("application");

                try {
                        Closure closure = new Closure(gse) {

                                public Object call() {
                                        try {
                                                RequestImpl request = (RequestImpl) binding.getVariable("request");
                                                ResponseImpl response = (ResponseImpl) binding.getVariable("response");
                                                String groovyFileName = requestURI.replace(altExtension, groovyExtension);
                                                RequestThreadInfo.get().setCurrentFile((RequestThreadInfo.get().getApplication().getAppPath() + File.separator + groovyFileName).replace("/", File.separator));

                                                ((GSE4) getDelegate()).run(groovyFileName, binding);

                                                Boolean alreadyForwarded = (Boolean) request.getAttribute("_explicitForward");

                                                if ((response.getBufferContentSize() == null || response.getBufferContentSize() == 0) && alreadyForwarded == false) {
                                                        String templatePath = requestURI.replace(altExtension, viewExtension);

                                                        request.forwardToView(templatePath);
//                                                      int i = scriptPath.lastIndexOf('/');
//                                                        if (i == -1)
//                                                                //replace .groovy w/ .gsp
//                                                                request.forward(scriptPath.replace(altExtension, viewExtension));
//                                                        else
//                                                                request.forward(scriptPath.substring(i + 1, scriptPath.length()).replace(altExtension, viewExtension));
                                                }

                                                return null;

                                        } catch (MissingPropertyException e) {
                                                response.clearBuffer();
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, requestURI, gse, binding, e);
                                        } catch (MissingMethodException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, requestURI, gse, binding, e);

                                        } catch (ResourceException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, requestURI, gse, binding, e);

                                        } catch (ScriptException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                if (e.getCause() instanceof FileNotFoundException) {
                                                        sendError(404, requestURI, gse, binding, e);
                                                } else {
                                                        sendError(500, requestURI, gse, binding, e);
                                                }
                                        } catch (IOException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, requestURI, gse, binding, e);

                                        } catch (ServletException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(500, requestURI, gse, binding, e);
                                        } catch (ThreadDeath e) {

                                                log.fine("Thread killed the hard way. script: " + requestURI);
                                                sendError(500, requestURI, gse, binding, new Exception("Thread exceeded max allowed time limit.", e));

                                        } catch (Throwable e) {
                                                log.log(Level.SEVERE, e.getMessage(), e);
                                                sendError(500, requestURI, gse, binding, e);
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
                        sendError(500, requestURI, gse, binding, e);
                } catch (SecurityException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        sendError(500, requestURI, gse, binding, e);
                } catch (TemplateNotFoundException e) {
                        log.log(Level.INFO, e.getMessage(), e);
                        sendError(500, requestURI, gse, binding, e);
                } catch (Throwable e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        sendError(500, requestURI, gse, binding, e);
                }
        }


        public static void processTemplateRequest(final String scriptPath, final GSE4 gse, final CustomServletBinding binding) {
                ResponseImpl response = (ResponseImpl) binding.getVariable("response");

                try {
                        Closure closure = new Closure(gse) {

                                public Object call() {
                                        try {
                                                if (!RequestThreadInfo.get().errorOccurred())
                                                        RequestThreadInfo.get().setCurrentFile(scriptPath);

                                                RequestImpl request = (RequestImpl) binding.getVariable("request");
                                                ResponseImpl response = (ResponseImpl) binding.getVariable("response");
                                                ServletContextImpl application = (ServletContextImpl) binding.getVariable("application");


                                                //application.getTemplateServlet().service(scriptPath, request, response, binding);
                                                application.getTemplateServlet().service(request, response);
//
//                                                ((GSE4) getDelegate()).run(scriptPath.replace(altExtension, groovyExtension), binding);
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
                                        } catch (FileNotFoundException e) {
                                                log.log(Level.FINE, e.getMessage(), e);
                                                sendError(404, scriptPath, gse, binding, e);
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
                                        } catch (ThreadDeath e) {
                                                log.fine("Thread killed the hard way. script: " + scriptPath);
                                                sendError(500, scriptPath, gse, binding, new Exception("Thread exceeded max allowed time limit.", e));
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

//        public static void processTemplateRequest(final String scriptPath, final GSE4 gse, final CustomServletBinding binding) {
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

        protected static void sendError(int errorCode, String scriptPath, GSE4 gse, CustomServletBinding binding, Throwable e) {
                if (RequestThreadInfo.get().errorOccurred()) {
                        return;
                }

                try {
                        ResponseImpl response = (ResponseImpl) binding.getVariable("response");
                        response.flushWriter();
                        response.clearBuffer();

                        RequestError requestError = RequestThreadInfo.get().getRequestError();
                        ServletContextImpl application = (ServletContextImpl) binding.getVariable("application");
                        RequestImpl request = (RequestImpl) binding.getVariable("request");
                        StackTraceElement stackTraceElement = findErrorInStackTrace(binding, e);

                        RequestThreadInfo.get().getTemplateInfo().setTemplateRoot(null);
                        String appPath = RequestThreadInfo.get().getApplication().getAppPath() + File.separator;
                        String appPath2 = "/" + RequestThreadInfo.get().getParsedRequest().getAppPath() + "/";

                        RequestThreadInfo.get().setErrorOccurred(true);
                        response.setStatus(errorCode, e.getMessage());

                        requestError.setErrorCode(errorCode);
                        requestError.setScriptPath(RequestThreadInfo.get().getCurrentFile());
                        if (e.getMessage() == null)
                                requestError.setErrorMessage("");
                        else
                                requestError.setErrorMessage(e.getMessage().replace(appPath, "").replace(appPath2, ""));

                        requestError.setException(e, appPath, appPath2);

                        binding.setVariable("exceptionName", e.getClass().getName());
                        binding.setVariable("requestError", requestError);

                        if (stackTraceElement != null) {
                                try {
                                        File f = new File(RequestThreadInfo.get().getCurrentFile());
                                        BufferedReader br = new BufferedReader(new FileReader(f));
                                        String line = null;
                                        StringBuffer lineBuffer = new StringBuffer();
                                        int lineCt = 1;

                                        while ((line = br.readLine()) != null) {
                                                if (lineCt >= stackTraceElement.getLineNumber() - 10 && (lineCt <= stackTraceElement.getLineNumber() + 10)) {
                                                        lineBuffer.append("<div class=\"sourceLine\"><div class=\"sourceLineNumber\">").append(lineCt).append("</div>");
                                                        lineBuffer.append("<div class=\"sourceCode\">");
                                                        lineBuffer.append(line.length() == 0 ? "&nbsp;" : StaticControllerMethods.htmlEncode(line).replaceAll(" ", "&nbsp;"));
                                                        lineBuffer.append("</div></div>");
                                                }

                                                lineCt++;
                                        }
                                        requestError.setSource(lineBuffer.toString());
                                        br.close();
                                } catch (Exception ex) {
                                        log.log(Level.SEVERE, "Unable to parse source code to display on error page", ex);
                                }
                        }

                        String path = RequestThreadInfo.get().getCurrentFile().replace(appPath, "").replace(appPath2, "");
                        if (stackTraceElement != null) {
//
//                                if (RequestThreadInfo.get().isTemplateRequest()) {
//                                        path = path.replace(appPath, "");
//                                }

                                String lineNumberMessage = "Error occurred in <span class=\"path\">" + path + "</span> @ lineNumber: " + (stackTraceElement.getLineNumber());
                                requestError.setLineNumberMessage(lineNumberMessage);
                                requestError.setLineNumber(stackTraceElement.getLineNumber());
                        } else {
                                if (RequestThreadInfo.get().isTemplateRequest()) {
                                        String lineNumberMessage = "Error occurred in <span class=\"path\">" + path + "</span>";
                                        requestError.setLineNumberMessage(lineNumberMessage);
                                } else {
                                        requestError.setLineNumberMessage("");
                                }
                                requestError.setLineNumber(-1);
                        }

                        if (application.hasCustomErrorFile("error" + errorCode + templateExtension)) {
                                String errorScriptPath = RequestThreadInfo.get().getApplication().getAppPath() + File.separator + "WEB-INF" + File.separator + "errors" + File.separator + "error" + errorCode + templateExtension;
                                RequestThreadInfo.get().getParsedRequest().setRequestFilePath(errorScriptPath);
                                RequestThread.processTemplateRequest(errorScriptPath, gse, binding);
                        } else {
                                String errorScriptPath = EasyGServer.APP_DIR + File.separator + "conf" + File.separator + "errors" + File.separator + "error" + errorCode + templateExtension;
                                RequestThreadInfo.get().getParsedRequest().setRequestFilePath(errorScriptPath);
                                RequestThread.processTemplateRequest(errorScriptPath, gse, binding);
                                //sendError(errorCode, response, e, stackTraceElement, binding);
                        }
                        //response.flushBuffer();

                } catch (Throwable e1) {
                        log.log(Level.FINE, e1.getMessage(), e1);
                        //sendError(errorCode, response, e, stackTraceElement, binding);
                }
        }

//         public void sendError(String message) {
//                RequestThreadInfo.get().getTemplateInfo().setTemplateRoot(null);
//                try {
//                        String content = "<html><head><title>EasyGSP Error</title></head><body>"  + message + "</body></html>";
//                        response.setStatus(500, "");
//                        response.getWriter().print(message);
//
//                } catch (Exception e1) {
//                        log.log(SEVERE, e1.getMessage(), e1);
//                }
//        }

        private static StackTraceElement findErrorInStackTrace(CustomServletBinding binding, Throwable e) {
                // File f = null;
                // f = new File(RequestThreadInfo.get().getCurrentFile());

                if (RequestThreadInfo.get().isTemplateRequest()) {
                        if (e.getStackTrace() != null && RequestThreadInfo.get().getUniqueTemplateScriptName() != null) {
                                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                                        if (stackTraceElement.getFileName() != null) {
                                                if (stackTraceElement.getFileName().startsWith(RequestThreadInfo.get().getUniqueTemplateScriptName())) {
                                                        return stackTraceElement;
                                                }
                                        }
                                }
                        }
                } else {
                        String appPath = RequestThreadInfo.get().getApplication().getAppPath();
                        String currentFile = RequestThreadInfo.get().getCurrentFile().substring(appPath.length() + 1);
                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                                if (stackTraceElement.getFileName() != null) {
                                        if (stackTraceElement.getFileName().endsWith(currentFile)) {
                                                return stackTraceElement;
                                        }
                                }
                        }
                }


                return null;
        }

//        public void sendError(String message) {
//                RequestThreadInfo.get().getTemplateInfo().setTemplateRoot(null);
//                try {
//                        String content = getErrorFileContent(500);
//                        response.setStatus(500, "");
//                        response.getWriter().print(message);
//
//                } catch (Exception e1) {
//                        log.log(SEVERE, e1.getMessage(), e1);
//                }
//        }

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


        public CustomServletBinding getBinding() {
                return binding;
        }

        public String getScriptPath() {
                return scriptPath;
        }

        public ServletContextImpl getApplication() {
                return application;
        }

}
