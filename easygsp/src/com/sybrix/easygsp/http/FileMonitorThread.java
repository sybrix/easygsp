package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;

/**
 * FileMonitorThread <br/>
 *
 * @author David Lee
 */
public class FileMonitorThread extends Thread {
        private static final Logger log = Logger.getLogger(FileMonitorThread.class.getName());

        //public static volatile List<ServletContextImpl> apps;
        private static volatile boolean stopThread = false;
        private static int interval;
        private static boolean found = false;

        public FileMonitorThread() {
                //this.apps = Collections.synchronizedList(new ArrayList());
                interval = EasyGServer.propertiesFile.getInt("file.monitor.interval", 3) * 1000;
        }

        @Override
        public void run() {
                log.fine("Manual FileMonitor thread started...");

                while (!stopThread) {

                        try {
                                Thread.sleep(interval);
                        } catch (InterruptedException e) {

                        }

                        if (stopThread)
                                break;
                        log.finest("iterating filesystem for changes....");
                        for (ServletContextImpl app : FileMonitor.apps.values()) {
                                found = false;
                                File f = new File(app.getAppPath());
                                StringBuffer path = new StringBuffer();

                                findModifiedFiles(f, app.getLastFileCheck(), app.getAppPath() + File.separator + "WEB-INF", path);
                                if (found) {
                                        if (app.isStarted()) {
                                                RequestThreadInfo.get().setApplication(app);
                                                if (app.hasOnChangedMethod()) {
                                                        try {
                                                                app.invokeWebMethod("onChanged", new Object[]{app, path});
                                                        } catch (Exception e) {
                                                                app.log(e.getMessage(), e);
                                                        }
                                                }

                                                app.updateLastFileCheck();
                                                //app.restart();
                                        }

                                }
                        }
                        
                }

                log.fine("Manual FileMonitor thread stopped");
        }

        private boolean findModifiedFiles(File f, long lastTime, String appPrefix,StringBuffer path) {
                for (File o : f.listFiles()) {
                        if (o.isDirectory()) {
                                findModifiedFiles(o, lastTime, appPrefix, path);
                        } else {
                                log.finest("checking - " + o.getAbsolutePath());
                                if ((o.lastModified() > lastTime) && o.getAbsolutePath().startsWith(appPrefix) && o.getAbsolutePath().endsWith(".groovy")) {
                                        log.fine("modified file found, " + o.getAbsolutePath());
                                        path.append(o.getPath());
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
