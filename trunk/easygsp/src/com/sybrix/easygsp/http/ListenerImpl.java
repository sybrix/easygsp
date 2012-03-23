package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;
import com.sybrix.easygsp.util.StringUtil;
import net.contentobjects.jnotify.JNotifyListener;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ListenerImpl implements JNotifyListener {
        private static final Logger logger = Logger.getLogger(FileMonitorThread.class.getName());

        private static Map<String, Long> fileTime = Collections.synchronizedMap(new HashMap());

        private boolean monitorGroovyFiles = EasyGServer.propertiesFile.getBoolean("file.monitor.groovy.files", false);
        private boolean monitorGSPFiles = EasyGServer.propertiesFile.getBoolean("file.monitor.gsp.files", false);
        private boolean restartOnChange = EasyGServer.propertiesFile.getBoolean("restart.onchange", false);

        public static Map<String, ServletContextImpl> apps;

        public ListenerImpl(Map apps) {
                this.apps = apps;
        }

        public void fileCreated(int i, String s, String s1) {
                s = s;
        }

        public void fileDeleted(int i, String s, String s1) {
                logger.finest("fileDeleted: " + s1);

                if (s1.endsWith(".gsp") || s1.endsWith(".gspx")) {
                        removeFromTemplateCache(s, s1);
                }
        }

        public void fileModified(int i, String path, String fileName) {

                Long lastTime = fileTime.get(fileName);
                Long now = System.currentTimeMillis();
                if (lastTime == null) {
                        fileTime.put(fileName, now);
                } else if ((now - lastTime) < 3000) {
                        return;
                } else {
                        fileTime.put(fileName, System.currentTimeMillis());
                }

                try {
                        logger.finer(" file monitor path: " + path + ", fileModified: " + fileName + ", monitorGroovyFiles:" + monitorGroovyFiles);

                        if (fileName.endsWith(".groovy") && monitorGroovyFiles) {


                                String p[] = fileName.split(File.separatorChar == '/' ? File.separator : (File.separator + File.separator));

                                if (p.length > 1) {

                                        if (p[1].equals("WEB-INF") && !p[p.length - 1].equals("web.groovy")) {

                                                ServletContextImpl app = apps.get(p[0]);
                                                if (app == null)
                                                        return;

                                                if (app.isStarted()) {
                                                        RequestThreadInfo.get().setApplication(app);
                                                        if (restartOnChange) {
                                                                app.restart();
                                                        } else {
                                                                if (app.hasOnChangedMethod()) {
                                                                        app.invokeWebMethod("onChanged", new Object[]{app, fileName});
                                                                }
                                                        }
                                                }
                                                //} else if (p[1].equals("WEB-INF") && p[p.length - 1].equals("web.groovy")) {

                                        }
                                }
                        } else if ((fileName.endsWith(".gsp") || fileName.endsWith(".gspx")) && monitorGSPFiles) {
                                removeFromTemplateCache(path, fileName);
                        }
                } catch (Throwable e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                }
        }

        private void removeFromTemplateCache(String s, String s1) {
                logger.finer(" template file path: " + s + ", template Modified: " + s1);
                String modifiedPath = EasyGServer.isWindows ? StringUtil.capDriveLetter(s.replaceAll("/", "\\\\")) : s;

                String p[] = s1.split(File.separatorChar == '/' ? File.separator : (File.separator + File.separator));

                if (p.length > 1) {
                        String path = modifiedPath + File.separator + p[0] + File.separator + "WEB-INF";
                        logger.finer("template path: " + path);
                        if (new File(path).exists()) {
                                ServletContextImpl app = apps.get(p[0]);
                                if (app != null) {
                                        app.getTemplateServlet().removeFromCache(modifiedPath + File.separator + s1);
                                        logger.fine("removing template cache entry: " + s1);
                                }
                        }
                }
        }

        public void fileRenamed(int i, String s, String s1, String s2) {
                s = s;

        }
}

