/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sybrix.easygsp.server;

import com.sybrix.easygsp.http.RequestThread;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.FileHandler;

import java.io.File;
import java.io.IOException;

import com.sybrix.easygsp.http.Application;
import com.sybrix.easygsp.http.SessionMonitor;
import com.sybrix.easygsp.http.LoggerThread;
import com.sybrix.easygsp.util.PropertiesFile;
import com.sybrix.easygsp.util.CustomLogFormatter;
import com.sybrix.easygsp.exception.ApplicationNotFoundException;
import org.apache.jcs.JCS;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.access.exception.CacheException;


public class EasyGServer {
        private static final Logger log = Logger.getLogger(EasyGServer.class.getName());

        private ServerSocket serverSocket;
        private ExecutorService executorService;
        private boolean isRunning;

        private ConcurrentHashMap<String, Application> applications = new ConcurrentHashMap();
        public static PropertiesFile propertiesFile;
        public static boolean isWindows;

        private static volatile boolean stopRequested = false;

        //private static String outputDir;
        private static String serverDir;
        public static String APP_DIR;
        private SessionMonitor sessionMonitor;

        static {
                //APP_DIR = System.getProperty("easygsp.home");
        }

        public EasyGServer(String groovyVersion) {
                try {
                        isWindows = System.getProperty("os.name").toLowerCase().contains("windows") || System.getProperty("os.name").toLowerCase().contains("winnt");

                        APP_DIR = System.getProperty("easygsp.home");
                        if (APP_DIR == null) {
                                throw new RuntimeException("-Deasygsp.home start up parameter not found");
                        }
                        if (APP_DIR.endsWith(File.separator)) {
                                APP_DIR = APP_DIR.substring(0, APP_DIR.length() - 1);
                        }

                        propertiesFile = new PropertiesFile(APP_DIR + File.separator + "conf" + File.separator + "server.properties");
                        System.setProperty("jcs.auxiliary.DC.attributes.DiskPath", APP_DIR + File.separator + "cache");

                        JCS.setConfigFilename(APP_DIR + File.separator + "conf" + File.separator + "cache.ccf");

                        System.setSecurityManager(new EasyGSecurityManager());

//                        log.fine("log fine");
//                        log.finer("log finer");
//                        log.finest("log finest");
//                        log.warning("log warn");
//                        log.info("log info");
//                        log.severe("log severe");

                        log.info("\nJRE_HOME: " + System.getProperty("java.home") +
                                "\nJAVA_VERSION: " + System.getProperty("java.version") +
                                "\nGROOVY_VERSION: " + groovyVersion +
                                "\nWORKING_DIR: " + APP_DIR +
                                "\nGroovy Script Server Started. Listening on port " + propertiesFile.getString("server.port", 4444) +
                                "\n");

                        if (EasyGServer.propertiesFile.getBoolean("virtual.hosting", false) == true && EasyGServer.propertiesFile.getString("default.host","").equals("")){
                                throw new RuntimeException("When virtual.hosting == true, a default.host value is required.");
                        }
                                
                        if (!propertiesFile.getBoolean("logging.custom.configure",false)){
                                autoConfigureLogger();
                        }
                        
                        ThreadMonitor.start();
                        sessionMonitor = new SessionMonitor(applications);
                        sessionMonitor.start();

                        LoggerThread loggerThread = new LoggerThread();
                        loggerThread.start();

                        serverDir = APP_DIR + File.separator + "lib";

                        //loadApplicationsFromFileSystem();

                        executorService = Executors.newCachedThreadPool();
                        serverSocket = new ServerSocket(propertiesFile.getInt("server.port","4444"));

                        Thread shutdown = new Thread(new Shutdown(this));
                        shutdown.start();

                        Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));


                        loadDatabaseDrivers();


                        try {
                                JCS.getInstance("appCache").get("");
                                JCS.getInstance("sessionCache").get("");

                        } catch (CacheException e) {
                                log.log(Level.SEVERE, "error initializing cache", e);
                        }


                        Socket socket = null;
                        while (!stopRequested) {
                                //executorService.execute(new RequestThread(serverSocket.accept(), applications));
                                try {
                                        socket = serverSocket.accept();
                                        if (isStopped())
                                                break;

                                        RequestThread t = new RequestThread(socket, applications);
                                        ThreadMonitor.add(t);
                                        t.start();

                                } catch (Exception e) {
                                        close(socket);
                                        log.log(Level.FINE, "server socket loop failed");
                                }

                        }

                        log.info("shutting down...");

                        while (!ThreadMonitor.isEmpty()) {
                                Thread.sleep(100);
                                log.fine("thread monitor size:" + ThreadMonitor.size());
                        }

                        loggerThread.stopLogging();
                        ThreadMonitor.stopMonitoring();

                        for (Application app : applications.values()) {
                                app.stopApplication();
                        }


                        sessionMonitor.stopThread();
                        while (sessionMonitor.isAlive() && loggerThread.messagesInQueue()) {
                                Thread.sleep(100);
                        }

                        JCS.getInstance("sessionCache").dispose();
                        JCS.getInstance("appCache").dispose();

                        CompositeCacheManager.getInstance().shutDown();

                        log.info("shutdown complete");
                        log.fine("stopped");
                        System.exit(0);
                } catch (Exception e) {
                        log.log(Level.SEVERE, "EasyGSP Server startup failed.", e);
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
                                handler.setLevel(Level.parse(propertiesFile.getString("logging.level","SEVERE").toUpperCase()));
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
                while (propertiesFile.get("database.driver." + index) != null) {
                        try {

                                driver = propertiesFile.getString("database.driver." + index++);
                                log.fine("loading database driver:" + driver);
                                Class.forName(driver);
                        } catch (ClassNotFoundException e) {
                                log.severe("Unable to load database driver: " + driver);
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

        public static Application loadApplicationFromFileSystem(Map<String, Application> applications, String appName, String appPath) throws ApplicationNotFoundException {
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

                log.fine("loading application: " + file.getAbsoluteFile());
                applications.put(appName, new Application(file));


                return applications.get(appName);
        }

        public static void main(String arg[]) {
                String groovyVersion = org.codehaus.groovy.runtime.InvokerHelper.getVersion();

                new EasyGServer(groovyVersion);
        }

        public static String getServerDir() {
                return serverDir;
        }

        public static void setServerDir(String serverDir) {
                EasyGServer.serverDir = serverDir;
        }

        protected void stopServer() {
                stopRequested = true;
        }

        public static boolean isStopped() {
                return stopRequested;
        }
}
