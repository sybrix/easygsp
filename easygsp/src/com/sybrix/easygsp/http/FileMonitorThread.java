package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;
import groovy.servlet.ServletCategory;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileMonitorThread <br/>
 *
 * @author David Lee
 */
public class FileMonitorThread extends Thread {
        private static final Logger logger = Logger.getLogger(FileMonitorThread.class.getName());

        public static volatile Map<String, ServletContextImpl> apps;
        private static volatile boolean stopThread = false;
        private static int interval;
        private static boolean found = false;

        public FileMonitorThread(Map apps) {
                this.apps = apps;//Collections.synchronizedList(new ArrayList());
                interval = EasyGServer.propertiesFile.getInt("file.monitor.interval", 3) * 1000;
        }

        @Override
        public void run() {
                logger.fine("Manual FileMonitor thread started...");

                ListenerImpl listener = new ListenerImpl(apps);

                while (!stopThread) {

                        try {
                                Thread.sleep(interval);
                        } catch (InterruptedException e) {

                        }

                        if (stopThread)
                                break;


                        logger.finest("iterating filesystem for changes....");
                        for (ServletContextImpl app : apps.values()) {

                                File f = new File(app.getAppPath());

                                findModifiedFiles(listener, f, app.getLastFileCheck(), app.getAppPath() + File.separator + "WEB-INF", app);
//                                if (found != null) {
//                                        listener.fileModified(listener, 0, found.getAbsolutePath(), found.getName());
//                                }

                                app.updateLastFileCheck();
                        }
                }

                logger.fine("Manual FileMonitor thread stopped");
        }

        private File findModifiedFiles(ListenerImpl listener, File f, long lastTime, String appPrefix, ServletContextImpl app) {

                for (File file : f.listFiles()) {
                        if (file.isDirectory()) {
                                findModifiedFiles(listener, file, lastTime, appPrefix, app);
                        } else {
                                logger.finest("checking - " + file.getAbsolutePath() + " file.lastModified=" + file.lastModified() + ", " +
                                        " lastTime=" + lastTime);

                                if ((file.lastModified() > lastTime) //
                                        && (file.getAbsolutePath().endsWith(".groovy") || file.getAbsolutePath()
                                        .endsWith(".gspx") || file.getAbsolutePath().endsWith(".gsp"))) {

                                        logger.finer("modified file found, " + file.getAbsolutePath());
                                        int lastSlash = app.getAppPath().lastIndexOf(File.separatorChar);

                                        try {
                                                listener.fileModified(0, file.getAbsolutePath().substring(0, lastSlash), file.getAbsolutePath().substring(lastSlash + 1));
                                        } catch (Throwable e) {
                                                logger.log(Level.FINER, " listener.fileModified failed. ", e);
                                        }
                                        return file;
                                }
                        }
                }

                return null;
        }

        public void stopThread() {
                this.stopThread = true;
                logger.fine("FileMonitor thread stop requested...");
                this.interrupt();
        }
}
