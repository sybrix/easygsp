package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
import java.io.File;

/**
 * FileMonitorThread <br/>
 *
 * @author David Lee
 */
public class FileMonitorThread extends Thread {
        private static final Logger log = Logger.getLogger(FileMonitorThread.class.getName());

        private static volatile List<ServletContextImpl> apps;
        private static volatile boolean stopThread = false;
        private static int interval;
        private static boolean found = false;

        public FileMonitorThread() {
                this.apps = Collections.synchronizedList(new ArrayList());
                interval = EasyGServer.propertiesFile.getInt("file.monitor.interval", 5) * 1000;
        }

        public static void addApp(ServletContextImpl app) {
                apps.add(app);
        }


        @Override
        public void run() {
                log.fine("FileMonitor thread started...");

                while (!stopThread) {

                        try {
                                Thread.sleep(interval);
                        } catch (InterruptedException e) {

                        }

                        if (stopThread)
                                break;
                        log.fine("iterating filesystem for changes....");
                        for (ServletContextImpl app : apps) {
                                found = false;
                                File f = new File(app.getAppPath());
                                findModifiedFiles(f, app.getLastFileCheck(), app.getAppPath() + File.separator + "WEB-INF");
                                if (found) {
                                        app.updateLastFileCheck();
                                        app.restart();
                                }

                        }
                }

                log.fine("FileMonitor thread stopped");
        }

        private boolean findModifiedFiles(File f, long lastTime, String appPrefix) {
                for (File o : f.listFiles()) {
                        if (o.isDirectory()) {
                                findModifiedFiles(o, lastTime, appPrefix);
                        } else {
                                log.finest("checking - " + o.getAbsolutePath());
                                if ((o.lastModified() > lastTime) && o.getAbsolutePath().startsWith(appPrefix) && o.getAbsolutePath().endsWith(".groovy")) {
                                        log.fine("modified file found, " + o.getAbsolutePath());
                                        found = true;
                                        break;
                                }
                        }
                }

                return found;
        }

        public void stopThread() {
                this.stopThread = stopThread;
                log.fine("FileMonitor thread stop requested...");
                this.interrupt();
        }
}
