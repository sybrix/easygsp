

/*
 * Copyright 2012. the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.sybrix.easygsp.server;

import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.FileHandler;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import com.sybrix.easygsp.email.EmailService;
import com.sybrix.easygsp.http.*;
import com.sybrix.easygsp.logging.LoggerThread;
import com.sybrix.easygsp.util.PropertiesFile;
import com.sybrix.easygsp.util.CustomLogFormatter;
import com.sybrix.easygsp.exception.ApplicationNotFoundException;
import groovy.util.GroovyScriptEngine;
import org.jgroups.*;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.util.Util;
import groovy.util.ScriptException;
import groovy.util.ResourceException;


public class EasyGServer extends ReceiverAdapter {
        private static final Logger logger = Logger.getLogger(EasyGServer.class.getName());
        public final static List<Class> categoryList = new ArrayList();

        public static JChannel jgroupsChannel;

        private ServerSocket serverSocket;

        private ConcurrentHashMap<String, ServletContextImpl> applications = new ConcurrentHashMap();

        public static Map<AppId, Set<String>> loadedScripts = new ConcurrentHashMap();
        public static PropertiesFile propertiesFile;
        public static boolean isWindows;

        private static volatile boolean stopRequested = false;

        private static String serverDir;
        public static String APP_DIR;
        private SessionMonitor sessionMonitor;
        private String groovyVersion;
        private ConsoleServer consoleServer;
        private static Boolean clusteringEnabled = false;
        private static volatile Boolean isSettingState = false;
        public static String adminApp;
        private FileMonitor fileMonitor;
        public static boolean gzipCompressionEnabled = false;

        public static Boolean restartOnChange;
        public static Boolean routingEnabled;

        static {
                //APP_DIR = System.getProperty("easygsp.home");
                categoryList.add(CustomServletCategory.class);

        }

        public EasyGServer() {
                groovyVersion = org.codehaus.groovy.runtime.InvokerHelper.getVersion();
        }

        public EasyGServer(String groovyVersion) {
                this.groovyVersion = groovyVersion;
        }

        public void start() {
                try {
                        APP_DIR = System.getProperty("easygsp.home");
                        if (APP_DIR == null) {
                                throw new RuntimeException("-Deasygsp.home start up parameter not found");
                        }
                        if (APP_DIR.endsWith(File.separator)) {
                                APP_DIR = APP_DIR.substring(0, APP_DIR.length() - 1);
                        }

                        String propFile = System.getProperty("easygsp.propFile");
                        if (propFile == null)
                                propertiesFile = new PropertiesFile(APP_DIR + File.separator + "conf" + File.separator + "server.properties");
                        else
                                propertiesFile = new PropertiesFile(propFile);


                        if (EasyGServer.propertiesFile.getInt("console.server.port", -1) > -1) {
                                consoleServer = new ConsoleServer();
                                consoleServer.start();
                        }

                        isWindows = System.getProperty("os.name").toLowerCase().contains("windows") || System.getProperty("os.name").toLowerCase().contains("winnt");
                        clusteringEnabled = propertiesFile.getBoolean("clustering.enabled", false);
                        adminApp = propertiesFile.getString("admin.app", "admin");
                        restartOnChange = propertiesFile.getBoolean("restart.onchange", false);
                        gzipCompressionEnabled = propertiesFile.getBoolean("gzip.compression.enabled",true);
                        routingEnabled = propertiesFile.getBoolean("url.routing.enabled", true);

                        if (System.getProperty("java.security.manager") != null) {
                                System.setSecurityManager(new EasyGSecurityManager());
                        } else {
                                logger.info("Skipped setting security manager");
                        }

                        System.setProperty("easygsp.version", "@easygsp_version");

                        logger.info(
                                "\nEASYGSP_VERSION: " + System.getProperty("easygsp.version") +
                                        "\nJRE_HOME: " + System.getProperty("java.home") +
                                        "\nJAVA_VERSION: " + System.getProperty("java.version") +
                                        "\nGROOVY_VERSION: " + groovyVersion +
                                        "\nWORKING_DIR: " + APP_DIR +
                                        "\nGroovy Script Server Started. Listening on port " + propertiesFile.getInt("server.port", 4444) +
                                        "\n");


                        if (EasyGServer.propertiesFile.getBoolean("virtual.hosting", false) == true && EasyGServer.propertiesFile.getString("default.host", "").equals("")) {
                                throw new RuntimeException("When virtual.hosting == true, a default.host value is required.");
                        }

                        if (!propertiesFile.getBoolean("logging.custom.configure", false)) {
                                autoConfigureLogger();
                        }

                        if (EasyGServer.propertiesFile.getBoolean("file.monitor.enabled", false)) {
                                //fm = new FileMonitorThread();
                                //fm.start();
                                fileMonitor = new FileMonitor(applications);
                                fileMonitor.listen();
                        }

                        ThreadMonitor.start();
                        sessionMonitor = new SessionMonitor(applications);
                        sessionMonitor.start();

                        EmailService.start();

                        LoggerThread loggerThread = new LoggerThread();
                        loggerThread.start();

                        serverDir = APP_DIR + File.separator + "lib";

                        //loadApplicationsFromFileSystem();

                       // executorService = Executors.newCachedThreadPool();
                        serverSocket = new ServerSocket(propertiesFile.getInt("server.port", 4444));
                        Thread shutdown = new Thread(new Shutdown(this));
                        shutdown.start();

                        Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));

                        loadDatabaseDrivers();

                        if (clusteringEnabled) {
                                if (propertiesFile.getString("cluster.config.file") == null)
                                        jgroupsChannel = new JChannel();
                                else {
                                        String path = APP_DIR + File.separator + "conf" + File.separator + propertiesFile.getString("cluster.config.file");
                                        File filePath = new File(path);
                                        if (!filePath.exists()) {
                                                filePath = new File(propertiesFile.getString("cluster.config.file"));
                                                if (!filePath.exists()) {
                                                        throw new Exception("Unable to find JGroups config file : " + propertiesFile.getString("cluster.config.file"));
                                                }
                                        }
                                        jgroupsChannel = new JChannel(path);
                                }

                                jgroupsChannel.setOpt(JChannel.LOCAL, false);
                                jgroupsChannel.setReceiver(this);
                                jgroupsChannel.connect(propertiesFile.getString("cluster.name", "EasyGSPCluster"));
                                jgroupsChannel.getState(null, 10000);
                                ClassConfigurator.add((short) 1899, AppHeader.class);

                                if (jgroupsChannel.getView().size() > 1 && isSettingState) {
                                        synchronized (serverSocket) {
                                                logger.fine("Clustering: waiting on cluster state transfer to complete before serverSocket listening");
                                                serverSocket.wait();
                                                logger.fine("Clustering: state transfer done");
                                        }
                                }
                        }

                        Socket socket = null;
                        logger.info("EasyGSP Server accepting connections");

                        //final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(500);
                       // WorkerThread workerThread = new WorkerThread();
                       // workerThread.start();

                        while (!stopRequested) {
                                try {
                                        if (isStopped())
                                                break;
  //                                      executorService.execute(new RequestThread2(serverSocket.accept(), applications));

                                        RequestThread t = new RequestThread(serverSocket.accept(), applications);

//                                        workerThread.setRequestThread(t);
//                                        workerThread.process();

                                        ThreadMonitor.add(t);
                                        t.start();
                                } catch (Exception e) {
                                        logger.log(Level.FINE, "server socket loop failed");
                                }
                        }

                        logger.info("shutting down...");

                        fileMonitor.stop();
                        EmailService.stop();

                        while (!ThreadMonitor.isEmpty()) {
                                Thread.sleep(100);
                                logger.fine("thread monitor size:" + ThreadMonitor.size());
                        }

                        ThreadMonitor.stopMonitoring();

                        for (ServletContextImpl app : applications.values()) {
                                app.stopApplication();
                        }

                        sessionMonitor.stopThread();
                        while (sessionMonitor.isAlive() && loggerThread.messagesInQueue()) {
                                Thread.sleep(100);
                        }

                        if (consoleServer != null)
                                consoleServer.stopThread();

                        if (clusteringEnabled) {
                                jgroupsChannel.close();
                        }

                        loggerThread.stopLogging();
                        while (loggerThread.messagesInQueue()) {
                                Thread.sleep(100);
                        }

                        logger.info("shutdown complete");
                        System.exit(0);
                } catch (Exception e) {
                        logger.log(Level.SEVERE, "EasyGSP Server startup failed.", e);
                        System.exit(0);
                }
        }

        protected ServerSocket getServerSocket() {
                stopServer();
                return serverSocket;
        }

        private void autoConfigureLogger() {
                Handler h = null;
                String logDir = "";
                try {
                        logDir = propertiesFile.getString("logging.output.dir", APP_DIR + File.separator + "logs" + File.separator + "error_%g.log");
                        h = new FileHandler(logDir, propertiesFile.getInt("logging.max.file.size"), propertiesFile.getInt("logging.file.count"), false);
                        h.setFormatter(new CustomLogFormatter());
                        Logger.getLogger("").addHandler(h);
                        Logger.getLogger("com.sybrix").setLevel(Level.parse(propertiesFile.getString("logging.level", "SEVERE").toString()));

                        Handler[] handlers = Logger.getLogger("").getHandlers();
                        for (Handler handler : handlers) {
                                //handler.setFormatter(new CustomFormatter());
                                handler.setLevel(Level.parse(propertiesFile.getString("logging.level", "SEVERE").toUpperCase()));
                        }
                } catch (IOException e) {
                        throw new RuntimeException("IOException in autoConfigureLogger(), logDir=" + logDir, e);
                } catch (Throwable e) {
                        throw new RuntimeException("Exception in autoConfigureLogger(), logDir=" + logDir, e);
                }
        }

        private void close(Object socket) {
                try {
                        if (socket instanceof Socket)
                                ((Socket) socket).close();
                        else if (socket instanceof ServerSocket)
                                ((ServerSocket) socket).close();
                } catch (Exception e) {

                }
        }

        private void loadDatabaseDrivers() {
                int index = 1;
                String driver = null;
                while (propertiesFile.getString("database.driver." + index) != null) {
                        try {

                                driver = propertiesFile.getString("database.driver." + index++);
                                logger.fine("loading database driver:" + driver);
                                Class.forName(driver);
                        } catch (ClassNotFoundException e) {
                                logger.severe("Unable to load database driver: " + driver);
                        }
                }
        }

//        private String parseAppFolderName(Map<String, String> headers) {
//                String scriptName = headers.get("SCRIPT_NAME");
//                return scriptName.substring(1, scriptName.indexOf('/', 1));
//        }

//        private void loadApplicationsFromFileSystem() {
//                File files = new File(propertiesFile.getString("groovy.webapp.dir"));
//                File dir[] = files.listFiles();
//
//                for (File file : dir) {
//                        if (file.isDirectory()) {
//                                log.fine("loading application: " + file.getAbsoluteFile());
//                                applications.put(file.getName(), new Application(file));
//                        }
//                }
//        }

        public static ServletContextImpl loadApplicationFromFileSystem(Map<String, ServletContextImpl> applications, String appName, String appPath) throws ApplicationNotFoundException {
                File file = null;

//                if (propertiesFile.getString("groovy.webapp.dir").equals(docRoot) || (EasyGServer.isWindows && propertiesFile.getString("groovy.webapp.dir").equalsIgnoreCase(docRoot))) {
                //File files = new File(appPath);
                file = new File(appPath);
//                } else {
//                        file = new File(docRoot);
//                }

                if (!file.exists()) {
                        throw new ApplicationNotFoundException(" application: " + appName + " not found");
                }

                logger.info("loading application: " + file.getAbsoluteFile());
                applications.put(appName, new ServletContextImpl(file));


                return applications.get(appName);
        }

        public static void main(String arg[]) {
                String groovyVersion = org.codehaus.groovy.runtime.InvokerHelper.getVersion();

                EasyGServer server = new EasyGServer(groovyVersion);
                server.start();
        }

        public static String getServerDir() {
                return serverDir;
        }

        public static void setServerDir(String serverDir) {
                EasyGServer.serverDir = serverDir;
        }

        void stopServer() {
                if (stopRequested)
                        return;

                stopRequested = true;
                try {
                        serverSocket.close();
                } catch (Exception e) {

                }
        }

        public static boolean isStopped() {
                return stopRequested;
        }

        // JGroups Stuff
        public static void sendToChannel(ClusterMessage sessionMessage) {
                if (!clusteringEnabled || isSettingState)
                        return;
                try {
                        Message msg = new Message(null, null, Util.objectToByteBuffer(sessionMessage));
                        msg.putHeader("ah", new AppHeader(sessionMessage.getAppId(), sessionMessage.getMethod(), sessionMessage.getAppPath()));
                        jgroupsChannel.send(msg);
                } catch (ChannelNotConnectedException e) {
                        logger.log(Level.SEVERE, "JGroups Exception, ChannelNotConnectedException", e);
                } catch (ChannelClosedException e) {
                        logger.log(Level.SEVERE, "JGroups Exception, ChannelClosedException", e);
                } catch (Exception e) {
                        logger.log(Level.SEVERE, "sendToChannel() Exception ", e);
                }
        }

        @Override
        public void receive(Message msg) {
                AppHeader appHeader = (AppHeader) msg.getHeader("ah");
                String[] app = appHeader.appName.split(";");
                String appName = app[0];
                String method = app[1];
                String appPath = app[2];

                ServletContextImpl application = applications.get(appName);
                SessionImpl session = null;
                try {
                        if (method.equals("appStart")) {
                                try {
                                        logger.fine("clustering: starting application - " + appName + ", path - " + appPath);
                                        application = loadApplication(appName, appPath);
                                } catch (ApplicationNotFoundException e) {
                                        logger.severe("Clustering Problem (appStart), ApplicationNotFoundException - application: " + appName + ", application path: " + appPath);
                                        return;
                                }

                        } else if (method.equals("session")) {

                                if (application == null) {
                                        try {
                                                logger.fine("clustering: starting application on session message -  " + appName + ", path - " + appPath);
                                                application = loadApplication(appName, appPath);
                                        } catch (ApplicationNotFoundException e) {
                                                logger.severe("Clustering Problem, ApplicationNotFoundException - application: " + appName);
                                                return;
                                        }
                                }

                                ClusterMessage sessionMessage = (ClusterMessage) getClusterMessageFromByteArray(msg.getRawBuffer(), application.getGroovyScriptEngine().getGroovyClassLoader());
                                session = (SessionImpl) sessionMessage.getParameters()[0];
                                session.setApplication(application);
                                application.getSessions().put(session.getId(), session);

                        } else if (method.equals("loadClass")) {
                                ClusterMessage clusterMessage = (ClusterMessage) getClusterMessageFromByteArray(msg.getRawBuffer(), application.getGroovyScriptEngine().getGroovyClassLoader());
                                String path = (String) clusterMessage.getParameters()[0];

                                if (application == null) {
                                        logger.fine("clustering: unable to loadClass class - " + path + ", for app: " + appName);
                                        return;
                                }


                                logger.fine("clustering: loading class - " + path + ", for app: " + appName);

                                GroovyScriptEngine gse = application.getGroovyScriptEngine();
                                gse.loadScriptByName(path);


                        } else {
//                        session = application.getSessions().get(sessionMessage.getSessionId());
//
//                        Class[] c = new Class[sessionMessage.getParameters().length];
//
//                        if (sessionMessage.getMethod().equals("remote_setAttribute")) {
//                                c[0] = String.class;
//                                c[1] = Object.class;
//                        } else {
//                                for (int i = 0; i < c.length; i++) {
//                                        c[i] = sessionMessage.getParameters()[i].getClass();
//                                }
//                        }
//
//                        try {
//                                Method method = session.getClass().getMethod(sessionMessage.getMethod(), c);
//                                method.invoke(session, sessionMessage.getParameters());
//                        } catch (NoSuchMethodException e) {
//                                log.log(Level.SEVERE, "Clustering problem, NoSuchMethodException for : " + e.getMessage(), e);
//                        } catch (InvocationTargetException e) {
//                                log.log(Level.SEVERE, "Clustering problem, InvocationTargetException : " + e.getMessage(), e);
//                        } catch (IllegalAccessException e) {
//                                log.log(Level.SEVERE, "Clustering problem, IllegalAccessException : " + e.getMessage(), e);
//                        }
                        }

                } catch (ScriptException e) {
                        logger.severe("Clustering Exception, ScriptException - application: " + appName + ", application path: " + appPath);
                } catch (ResourceException e) {
                        logger.severe("Clustering Exception, ResourceException - application: " + appName + ", application path: " + appPath);
                } catch (ClassNotFoundException e) {
                        logger.log(Level.SEVERE, "Clustering Exception, ClassNotFoundException", e);
                } catch (IOException e) {
                        logger.log(Level.SEVERE, "Clustering Exception, IOException", e);
                }

        }

        public static Object getClusterMessageFromByteArray(byte[] data, ClassLoader classLoader) throws IOException, ClassNotFoundException {
                GroovyObjectInputStream obj = null;

                ByteArrayInputStream ba = new ByteArrayInputStream(data);
                ba.read();
                obj = new GroovyObjectInputStream(classLoader, ba);

                Object o = obj.readObject();
                //ClusterMessage _sessionMessage = (ClusterMessage) msg.getObject();

                obj.close();

                return o;
        }


//        public ClusterMessage getClusterMessage(Message msg, ClassLoader classLoader) throws IOException, ClassNotFoundException {
//                ObjectInputStream obj = null;
//
//                ByteArrayInputStream ba = new ByteArrayInputStream(msg.getRawBuffer(), msg.getOffset(), msg.getLength());
//                ba.read();
//                obj = new GroovyObjectInputStream(classLoader, ba);
//
//                ClusterMessage sessionMessage = (ClusterMessage) obj.readObject();
//                //ClusterMessage _sessionMessage = (ClusterMessage) msg.getObject();
//
//                obj.close();
//
//                return sessionMessage;
//        }

        @Override
        public byte[] getState() {
                Map<AppId, byte[]> apps = new HashMap();
                logger.fine("Clustering: getState() - converting sessions to byte array");
                for (ServletContextImpl context : applications.values()) {
                        AppId id = new AppId(context.getAppName(), context.getAppPath());
                        try {
                                apps.put(id, Util.objectToByteBuffer(new ArrayList(context.getSessions().values())));
                        } catch (Exception e) {
                                logger.log(Level.SEVERE, "exception occurred converting sessions to byte array, app: " + id.getAppName(), e);
                        }
                }

                logger.fine("Clustering: getState() - converting request history to byte array");
                Map<AppId, byte[]> requests = new HashMap();
                for (AppId id : EasyGServer.loadedScripts.keySet()) {
                        try {
                                requests.put(id, Util.objectToByteBuffer(EasyGServer.loadedScripts.get(id)));
                        } catch (Exception e) {
                                logger.log(Level.SEVERE, "exception occurred converting request history to byte array, app: " + id.getAppName(), e);
                        }
                }

                Map<String, Map> state = new HashMap();
                state.put("sessions", apps);
                state.put("requests", requests);

                try {
                        return Util.objectToByteBuffer(state);
                } catch (Exception e) {
                        logger.log(Level.SEVERE, "Unable to getState(), " + e.getMessage(), e);
                }

                logger.fine("Clustering: done getting state");
                return new byte[]{};
        }

        @Override
        public void setState(byte[] state) {
                isSettingState = true;
                logger.fine("Clustering: setting state...");
                Map data = null;
                try {
                        data = (Map) Util.objectFromByteBuffer(state);
                } catch (Exception e) {
                        logger.log(Level.SEVERE, "unable to setState, Util.objectFromByteBuffer failed", e);
                        return;
                }

                Map<AppId, byte[]> requests = (Map) data.get("requests");

                logger.fine("Clustering: loading request history...");
                for (AppId id : requests.keySet()) {
                        try {
                                ServletContextImpl app = applications.get(id.getAppName());
                                if (app == null) {
                                        app = loadApplication(id.getAppName(), id.getAppPath());
                                }
                                Set<String> scripts = (Set) getClusterMessageFromByteArray((byte[]) requests.get(id), app.getGroovyScriptEngine().getGroovyClassLoader());
                                for (String script : scripts) {
                                        logger.fine("Clustering: loading script - " + script + ", app - " + app.getAppName());
                                        app.getGroovyScriptEngine().loadScriptByName(script);
                                }
                                EasyGServer.loadedScripts.remove(id);
                                EasyGServer.loadedScripts.put(id, scripts);
                        } catch (Exception e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                }

                Map<AppId, byte[]> apps = (Map) data.get("sessions");

                logger.fine("Clustering: loading sessions...");
                for (AppId id : apps.keySet()) {
                        try {
                                String appName = id.getAppName();
                                String appPath = id.getAppPath();

                                ServletContextImpl app = applications.get(appName);
                                if (app == null) {
                                        app = loadApplication(appName, appPath);
                                }

                                List<SessionImpl> sessions = (List) getClusterMessageFromByteArray(apps.get(id), app.getGroovyScriptEngine().getGroovyClassLoader());
                                for (SessionImpl session : sessions) {
                                        session.setApplication(app);
                                        app.getSessions().put(session.getId(), session);
                                        logger.fine("Clustering: loading session - " + session.getId() + ", app - " + app.getAppName());
                                }
                        } catch (Exception e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                }

                isSettingState = false;

                synchronized (serverSocket) {
                        serverSocket.notifyAll();
                }

                logger.fine("Clustering: done setting state");
        }

        private ServletContextImpl loadApplication(String appName, String appPath) throws ApplicationNotFoundException {
                ServletContextImpl app = loadApplicationFromFileSystem(applications, appName, appPath);
                RequestThreadInfo.get().setApplication(app);
                app.startApplication();
                return app;
        }

        @Override
        public void viewAccepted(View new_view) {
                System.out.println("** view: " + new_view);
        }

        public static void addScriptToState(String appName, String appPath, String scriptName) {
                if (clusteringEnabled) {
                        AppId id = new AppId(appName, appPath);
                        if (!loadedScripts.containsKey(id)) {
                                Set requests = Collections.synchronizedSet(new LinkedHashSet());
                                loadedScripts.put(id, requests);
                                loadedScripts.get(id).add(scriptName);
                        } else {
                                loadedScripts.get(id).add(scriptName);
                        }
                }
        }

}
