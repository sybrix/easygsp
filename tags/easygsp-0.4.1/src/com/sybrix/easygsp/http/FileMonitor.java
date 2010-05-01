package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;

import net.contentobjects.jnotify.JNotifyListener;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;

/**
 * FileMonitorThread <br/>
 *
 * @author David Lee
 */
public class FileMonitor {
        private static final Logger log = Logger.getLogger(FileMonitorThread.class.getName());

        public static Map<String, ServletContextImpl> apps;
        private static int watchId = 0;
        private static FileMonitorThread fileMonitorThread;
        private static boolean manualMonitor = EasyGServer.propertiesFile.getBoolean("use.manual.file.monitor", true);

        public static void listen() throws JNotifyException {
                if (!manualMonitor) {
                        int mask = JNotify.FILE_MODIFIED;
                        boolean watchSubtree = true;
                        String path = EasyGServer.propertiesFile.getString("file.monitor.path", "webapps").trim();

                        if ((path.charAt(0) == '/' && !EasyGServer.isWindows) || (EasyGServer.isWindows && path.charAt(1) == ':')) {
                                watchId = JNotify.addWatch(path, mask, watchSubtree, new ListenerImpl());
                        } else if ((path.charAt(0) != '/' && !EasyGServer.isWindows) || (EasyGServer.isWindows && path.charAt(1) != ':')) {
                                path = EasyGServer.APP_DIR + File.separator + path;
                                watchId = JNotify.addWatch(path, mask, watchSubtree, new ListenerImpl());
                        }

                        log.fine("FileMonitor watching path: " + path);
                } else {
                        fileMonitorThread = new FileMonitorThread();
                        fileMonitorThread.start();
                }
        }

        public static void stop() {
                if (!manualMonitor) {
                        if (watchId != 0) {
                                try {
                                        JNotify.removeWatch(watchId);
                                } catch (JNotifyException e) {

                                }
                        }
                } else {
                        fileMonitorThread.stopThread();
                }
        }

        static class ListenerImpl implements JNotifyListener {
                private static Map<String, Long> fileTime = Collections.synchronizedMap(new HashMap());

                public void fileCreated(int i, String s, String s1) {

                }

                public void fileDeleted(int i, String s, String s1) {
                        log.finest("fileDeleted: " + s1);

                        if (s1.endsWith(".gsp") || s1.endsWith(".gspx")) {
                                removeFromTemplateCache(s1);
                        }
                }

                public void fileModified(int i, String s, String s1) {
                        log.finest(" fileModified: " + s1);

                        Long lastTime = fileTime.get(s1);
                        if (lastTime == null) {
                                fileTime.put(s1, System.currentTimeMillis());
                        } else if ((System.currentTimeMillis() - lastTime) < 1000) {
                                return;
                        } else {
                                fileTime.put(s1, System.currentTimeMillis());
                        }


                        try {
                                if (s1.endsWith(".groovy")) {
                                        String p[] = s1.split(File.separatorChar == '/' ? File.separator : (File.separator + File.separator));
                                        if (p.length > 1) {
                                                if (p[1].equals("WEB-INF") && !s1.equals("web.groovy")) {

                                                        ServletContextImpl app = apps.get(p[0]);
                                                        if (app == null)
                                                                return;

                                                        if (app.isStarted()) {
                                                                log.fine("reloading classloader: " + p[0]);
                                                                app.restart();
                                                        }
                                                }
                                        }
                                } else if (s1.endsWith(".gsp") || s1.endsWith(".gspx")) {
                                        removeFromTemplateCache(s1);

                                }
                        } catch (Throwable e) {
                                log.log(Level.SEVERE, e.getMessage(), e);
                        }
                }

                private void removeFromTemplateCache(String s1) {
                        String p[] = s1.split(File.separatorChar == '/' ? File.separator : (File.separator + File.separator));
                        if (p.length > 1) {
                                if (new File(EasyGServer.APP_DIR + File.separator + "webapps" + File.separator + p[0] + File.separator + "WEB-INF").exists()) {
                                        ServletContextImpl app = apps.get(p[0]);
                                        if (app != null) {
                                                app.getTemplateServlet().removeFromCache(EasyGServer.APP_DIR + File.separator + "webapps" + File.separator + s1);
                                                log.fine("removing template cache entry: " + s1);
                                        }
                                }
                        }
                }

                public void fileRenamed(int i, String s, String s1, String s2) {


                }
        }
}
