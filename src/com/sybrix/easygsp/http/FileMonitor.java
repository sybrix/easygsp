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

package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

/**
 * FileMonitorThread <br/>
 *
 * @author David Lee
 */
public class FileMonitor {
        private static final Logger logger = Logger.getLogger(FileMonitorThread.class.getName());

        private  Map<String, ServletContextImpl> apps;
        private static int watchId = 0;
        private  FileMonitorThread fileMonitorThread;
        private  boolean manualMonitor = EasyGServer.propertiesFile.getBoolean("use.manual.file.monitor", true);
        private  boolean monitorGroovyFiles = EasyGServer.propertiesFile.getBoolean("file.monitor.groovy.files", false);

        public FileMonitor(Map apps) {
                this.apps = apps;

        }

        public  void listen() throws JNotifyException {
                if (!manualMonitor) {
                        int mask = JNotify.FILE_MODIFIED;
                        boolean watchSubtree = true;
                        String path = EasyGServer.propertiesFile.getString("file.monitor.path", "webapps").trim();

                        if ((path.charAt(0) == '/' && !EasyGServer.isWindows) || (EasyGServer.isWindows && path.charAt(1) == ':')) {
                                watchId = JNotify.addWatch(path, mask, watchSubtree, new ListenerImpl(apps));
                        } else if ((path.charAt(0) != '/' && !EasyGServer.isWindows) || (EasyGServer.isWindows && path.charAt(1) != ':')) {
                                path = EasyGServer.APP_DIR + File.separator + path;
                                watchId = JNotify.addWatch(path, mask, watchSubtree, new ListenerImpl(apps));
                        }

                        logger.info("FileMonitor watching path: " + path);
                } else {
                        fileMonitorThread = new FileMonitorThread(apps);
                        fileMonitorThread.start();
                }
        }

        public  void stop() {
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

//        static class ListenerImpl implements JNotifyListener {
//                private static Map<String, Long> fileTime = Collections.synchronizedMap(new HashMap());
//
//                public void fileCreated(int i, String s, String s1) {
//
//                }
//
//                public void fileDeleted(int i, String s, String s1) {
//                        logger.finest("fileDeleted: " + s1);
//
//                        if (s1.endsWith(".gsp") || s1.endsWith(".gspx")) {
//                                removeFromTemplateCache(s, s1);
//                        }
//                }
//
//                public void fileModified(int i, String s, String s1) {
//
//                        Long lastTime = fileTime.get(s1);
//                        Long now = System.currentTimeMillis();
//                        if (lastTime == null) {
//                                fileTime.put(s1, now);
//                        } else if ((now - lastTime) < 3000) {
//                                //System.out.println("skipped " + s1);
//                                return;
//                        } else {
//                                fileTime.put(s1, System.currentTimeMillis());
//                        }
//                        //System.out.println(s1);
//                        //System.out.println(lastTime == null ? now : "diff " + (now - lastTime));
//                        try {
//                                if (s1.endsWith(".groovy") && monitorGroovyFiles) {
//                                        String p[] = s1.split(File.separatorChar == '/' ? File.separator : (File.separator + File.separator));
//                                        if (p.length > 1) {
//                                                logger.finest(" file monitor path: " + s + ", fileModified: " + s1);
//
//                                                if (p[1].equals("WEB-INF") && !p[p.length - 1].equals("web.groovy")) {
//
//                                                        ServletContextImpl app = apps.get(p[0]);
//                                                        if (app == null)
//                                                                return;
//
//                                                        if (app.isStarted()) {
//                                                                // log.fine("reloading classloader: " + p[0]);
//                                                                // app.getGroovyScriptEngine().removeScriptCacheEntry("");
//                                                                // app.getGroovyScriptEngine().getGroovyClassLoader().clearCache();
//                                                                // Class c = app.getGroovyScriptEngine().loadScriptByName("reload.groovy");
//                                                                //log.fine("reloading classloader: " + p[0]);
//                                                                RequestThreadInfo.get().setApplication(app);
//                                                                String modifiedFile  = s + File.separator + s1;
//                                                                //RequestThreadInfo.get().getApplication().getGroovyScriptEngine().removeScriptCacheEntry("/" + modifiedFile.replaceAll("\\\\","/"));
//                                                                if (app.hasOnChangedMethod()){
//                                                                        app.invokeWebMethod("onChanged", new Object[]{app, modifiedFile});
//                                                                }
//                                                                //app.restart();
//                                                        }
//                                                }
//                                        }
//                                } else if (s1.endsWith(".gsp") || s1.endsWith(".gspx")) {
//                                        removeFromTemplateCache(s, s1);
//                                }
//                        } catch (Throwable e) {
//                                logger.log(Level.SEVERE, e.getMessage(), e);
//                        }
//                }
//
//                private void removeFromTemplateCache(String s, String s1) {
//                        logger.fine(" template file path: " + s + ", template Modified: " + s1);
//                        String modifiedPath = EasyGServer.isWindows ? StringUtil.capDriveLetter(s.replaceAll("/", "\\\\")) : s;
//
//                        String p[] = s1.split(File.separatorChar == '/' ? File.separator : (File.separator + File.separator));
//
//                        if (p.length > 1) {
//                                String path = modifiedPath + File.separator + p[0] + File.separator + "WEB-INF";
//                                if (new File(path).exists()) {
//                                        ServletContextImpl app = apps.get(p[0]);
//                                        if (app != null) {
//                                                app.getTemplateServlet().removeFromCache(modifiedPath + File.separator + s1);
//                                                logger.fine("removing template cache entry: " + s1);
//                                        }
//                                }
//                        }
//                }
//
//                public void fileRenamed(int i, String s, String s1, String s2) {
//
//
//                }
//        }
}
