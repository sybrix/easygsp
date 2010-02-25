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
package com.sybrix.easygsp.http;

import groovy.util.ScriptException;
import groovy.util.ResourceException;

import groovy.lang.GroovyObject;
import groovy.lang.Closure;


import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;

import com.sybrix.easygsp.exception.NotImplementedException;
import com.sybrix.easygsp.util.Hash;
import com.sybrix.easygsp.http.TemplateServlet;
import com.sybrix.easygsp.server.EasyGServer;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

/**
 * Application (aka ServletContext) <br/>
 * Description : EasyGSP ServletContext implementation.
 */
public class ServletContextImpl implements ServletContext, Serializable {
        private static final Logger log = Logger.getLogger(ServletContextImpl.class.getName());

        private String appPath;
        private GSE4 groovyScriptEngine;

        private TemplateServlet templateServlet;
        private boolean hasWebGroovy = false;
        private String appId;
        private volatile boolean started;
        private Set<String> attributeNames;
        private Map<String, SessionImpl> sessions;
        private String groovyFilePath;
        private File appFile;
        private String appName;
        private AttributeMap attributeMap;
        private long webGroovyLastModified;
        private ConcurrentHashMap resourceBundles;
        private List<String> errorFiles;

        public ServletContextImpl(File dir) {
                //this.appId = MD5.hash(dir);
                this.appFile = dir;
                this.appId = Hash.MD5(dir.getAbsolutePath());
                this.appPath = dir.getAbsolutePath();
                attributeNames = new HashSet();
                groovyFilePath = appPath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "web.groovy";
                File _groovyFile = new File(groovyFilePath);

                hasWebGroovy = _groovyFile.exists();
                if (hasWebGroovy) {
                        webGroovyLastModified = _groovyFile.lastModified();
                } else {
                        createWebGroovyFile(_groovyFile);
                        hasWebGroovy = true;
                }

                sessions = new ConcurrentHashMap();
                resourceBundles = new ConcurrentHashMap();

                appName = dir.getName();

                attributeMap = new AttributeMap();
                errorFiles = new ArrayList();
        }

        protected File getAppFile() {
                return appFile;
        }

        public String getGroovyFilePath() {
                return groovyFilePath;
        }

        public void setGroovyFilePath(String groovyFilePath) {
                this.groovyFilePath = groovyFilePath;
        }

        public boolean groovyWebFileExists() {
                return hasWebGroovy;
        }

        public String getAppName() {
                return appName;
        }

        public void setAppName(String appName) {
                this.appName = appName;
        }

        protected GSE4 getGroovyScriptEngine() {
                return groovyScriptEngine;
        }

        public String getAppPath() {
                return appPath;
        }

        public String getContextPath() {
                throw new NotImplementedException("Application.getContextPath() is not implemented");
        }

        public ServletContext getContext(String s) {
                throw new NotImplementedException("Application.getContext() is not implemented");
        }

        public int getMajorVersion() {
                return 0;
        }

        public int getMinorVersion() {
                return 0;
        }

        public String getMimeType(String s) {
                return null;
        }

        public Set getResourcePaths(String s) {
                throw new NotImplementedException("Application.getResourcePaths() is not implemented");
        }

        public URL getResource(String s) throws MalformedURLException {
                throw new NotImplementedException("Application.getResource() is not implemented");
        }

        public InputStream getResourceAsStream(String s) {
                throw new NotImplementedException("Application.getResourceAsStream() is not implemented");
        }

        public RequestDispatcher getRequestDispatcher(String s) {
                return null;
        }

        public RequestDispatcher getNamedDispatcher(String s) {
                return null;
        }

        public Servlet getServlet(String s) throws ServletException {
                throw new NotImplementedException("Application.getServlet() is not implemented");
        }

        public Enumeration getServlets() {
                throw new NotImplementedException("Application.getServlets() is not implemented");
        }

        public Enumeration getServletNames() {
                throw new NotImplementedException("Application.getServletNames() is not implemented");
        }

        public void log(String s) {
                throw new NotImplementedException("Application.log() is not implemented");
        }

        public void log(Exception e, String s) {
                throw new NotImplementedException("Application.log() is not implemented");
        }

        public void log(String s, Throwable throwable) {
                throw new NotImplementedException("Application.log() is not implemented");
        }

        public String getRealPath(String s) {
                return null;
        }

        public String getServerInfo() {
                return "Groovy Scripting Server 1.0";
        }

        public String getInitParameter(String s) {
                throw new NotImplementedException("Application.getInitParameter() is not implemented");
        }

        public Enumeration getInitParameterNames() {
                throw new NotImplementedException("Application.getInitParameterNames() is not implemented");
        }

        public Object getAttribute(String key) {
                return ApplicationCache.getInstance().get(appId, key);
        }

        public Enumeration getAttributeNames() {
                return Collections.enumeration(attributeNames);
        }

        protected Map getAttributes() {
                return attributeMap;
        }

        protected boolean hasCustomErrorFile(String fileName) {
                return errorFiles.contains(fileName);
        }

        boolean isStarted() {
                return started;
        }

        public ConcurrentHashMap getResourceBundles() {
                return resourceBundles;
        }

        public void setResourceBundles(ConcurrentHashMap resourceBundles) {
                this.resourceBundles = resourceBundles;
        }

        public boolean webGroovyUpdated() {
                File f = new File(groovyFilePath);
                if (f.exists()) {
                        if (f.lastModified() != webGroovyLastModified) {
                                webGroovyLastModified = f.lastModified();
                                return true;
                        }
                } else {
                        createWebGroovyFile(f);
                        return true;
                }

                return false;
        }

        protected void createWebGroovyFile(File f) {
                try {
                        log.fine("creating web.groovy @ " + f.getAbsolutePath());

                        log.fine("making dir " + f.getParentFile().getAbsolutePath());
                        f.getParentFile().mkdir();

                        String webGroovy =
                                "class web {\n" +
                                        "\t        def onApplicationStart(app){\n" +
                                        "\t        }\n\n" +

                                        "\t        def onApplicationEnd(app){\n" +
                                        "\t        }\n\n" +

                                        "\t        def onSessionStart(session){\n" +
                                        "\t        }\n\n" +

                                        "\t        def onSessionEnd(session){\n" +
                                        "\t       }\n" +
                                        "}";

                        FileWriter fw = new FileWriter(f);
                        fw.write(webGroovy);
                        fw.close();


                        webGroovyLastModified = f.lastModified();
                } catch (IOException e) {
                        log.log(Level.SEVERE, "Unable to create web.groovy @ " + f.getAbsolutePath(), e);
                }
        }

        public void setAttribute(String key, Object value) {
                ApplicationCache.getInstance().put(appId, key, value);
                attributeNames.add(key);
                CacheKeyManager.setAppKey(appName, key);
        }

        public void removeAttribute(String key) {
                ApplicationCache.getInstance().remove(appId, key);
                attributeNames.remove(key);
                CacheKeyManager.removeAppKey(appName, key);
        }

        public String getServletContextName() {
                return null;
        }

        protected synchronized void startApplication(boolean resumeState) {
                if (started)
                        return;

                try {

                        loadErrorFiles();
                        AppClassLoader parentClassLoader = new AppClassLoader(new URL[]{});
                        parentClassLoader.setAllowThreads(EasyGServer.propertiesFile.getBoolean("allow.threads", false));

                        //                                                Class gse = parentClassLoader.loadClass("groovy.util.GroovyScriptEngine");
                        //                                                Constructor con = gse.getConstructor(String[].class, ClassLoader.class);
                        //                                                groovyScriptEngine = (GSE3)con.newInstance(new String[]{appPath, appPath + System.getProperty("file.separator") + "WEB-INF"}, parentClassLoader);
                        groovyScriptEngine = new GSE4(new String[]{appPath, appPath + File.separator + "WEB-INF"}, parentClassLoader);
                        Map params = new HashMap();
                        templateServlet = new TemplateServlet(groovyScriptEngine);
                        log.fine("invoking onApplicationStart for " + appName);
                        //groovyScriptEngine = new GroovyScriptEngine(new String[]{appPath, appPath + System.getProperty("file.separator") + "WEB-INF"});


                        if (resumeState) {
                                reEstablishState();
                        }

                        if (hasWebGroovy) {
                                invokeWebMethod("onApplicationStart", this);
                        }

                        //                        groovyScriptEngine = new GroovyScriptEngine(new String[]{appPath});
                        //                        Class clazz = groovyScriptEngine.loadScriptByName("WEB-INF.web");
                        //                        GroovyObject o = (GroovyObject) clazz.newInstance();
                        //                        o.invokeMethod("onApplicationStart", new Object[]{this});
                        started = true;
                        //                } catch (IOException e) {
                        //                        log.log(Level.SEVERE, "onApplicationStart failed. IOException.", e);
                        //                } catch (ScriptException e) {
                        //                        log.log(Level.SEVERE, "onApplicationStart failed. ScriptException.", e);
                        //                } catch (ResourceException e) {
                        //                        log.log(Level.SEVERE, "onApplicationStart failed. ResourceException.", e);
                        //                } catch (IllegalAccessException e) {
                        //                        log.log(Level.SEVERE, "onApplicationStart failed. IllegalAccessException.", e);
                        //                } catch (InstantiationException e) {
                        //                        log.log(Level.SEVERE, "onApplicationStart failed. InstantiationException.", e);
                        log.fine("onApplicationStart successful for " + appName);
                } catch (Exception e) {
                        log.log(Level.FINE, "onApplicationStart failed.", e);
                }
        }

        public void reEstablishState() {
                List<String> keys = CacheKeyManager.getAllAppKeys(appName);
                for (String key : keys) {
                        attributeNames.add(key);
                }

                List<String> sessionIds = CacheKeyManager.getAllSessionIds(appName);
                for (String sessionId : sessionIds) {
                        recreateSession(sessionId);
                }
        }

        protected SessionImpl recreateSession(String sessionId) {
                SessionImpl session = new SessionImpl(this, sessionId, EasyGServer.propertiesFile.getInt("session.timeout", 15));
                sessions.put(sessionId, session);
                List<String> sessionKeys = CacheKeyManager.getAllSessionKeys(appName, session.getId());
                for (String sessionKey : sessionKeys) {
                        session.addSessionAttributeName(sessionKey);
                        Long lastAccessedTime = (Long) SessionCache.getInstance().get(session.getId(), "lastAccessedTime");
                        SessionCache.getInstance().remove(appName, session.getId(), "lastAccessedTime", false);
                        if (lastAccessedTime != null)
                                session.setLastAccessTime(lastAccessedTime);
                }

                return session;
        }


        private void loadErrorFiles() {
                errorFiles.clear();
                File f = new File(appPath + File.separator + "WEB-INF" + File.separator + "errors");

                File[] files = f.listFiles();
                if (files == null)
                        return;

                for (File file : files) {
                        errorFiles.add(file.getName());
                }

        }

        protected void invokeWebMethod(final String method, final Object... param) throws ClassNotFoundException,
                InstantiationException, IllegalAccessException, ScriptException, ResourceException {

                Closure closure = new Closure(groovyScriptEngine) {

                        public Object call() {
                                synchronized (groovyScriptEngine) {
                                        try {
                                                Class clazz = groovyScriptEngine.loadScriptByName("web.groovy");

                                                GroovyObject o = (GroovyObject) clazz.newInstance();
                                                o.invokeMethod(method, param);

                                        } catch (Throwable e) {
                                                log.log(Level.SEVERE, e.getMessage(), e);
                                        }

                                        groovyScriptEngine.notifyAll();
                                }
                                return null;
                        }

                };
                GroovyCategorySupport.use(CustomServletCategory.class, closure);


                //                Class cls = groovyClassLoader.loadClass("web");
                //                GroovyObject go = (GroovyObject)cls.newInstance();
                //                go.invokeMethod(method, param);
        }

        protected void killApp() {
                for (String attributeName : attributeNames) {
                        ApplicationCache.getInstance().remove(appId, attributeName);
                }
                CacheKeyManager.removeApp(appId);
                Object keys[] = sessions.keySet().toArray();
                for (Object key : keys) {
                        sessions.get(key.toString()).invalidate();
                        sessions.remove(key.toString());
                }
        }
        // should this hidden >

        public void stopApplication() {
                try {
                        if (hasWebGroovy) {
                                invokeWebMethod("onApplicationStop", this);
                        }
                        started = false;
                } catch (Exception e) {
                        log.log(Level.FINE, "onApplicationEnd failed.", e);
                }
        }

        protected Map<String, SessionImpl> getSessions() {
                return sessions;
        }

        public TemplateServlet getTemplateServlet() {
                return templateServlet;
        }

        public void setTemplateServlet(TemplateServlet templateServlet) {
                this.templateServlet = templateServlet;
        }

        public Class classForName(String name) {
                try {
                        ServletContextImpl app = RequestThreadInfo.get().getApplication();
                        return app.getGroovyScriptEngine().loadScriptByName(name);
                } catch (Exception e) {
                        throw new RuntimeException("Unable to load classFor: " + name, e);
                }
        }

        class AttributeMap implements Map {

                public int size() {
                        return attributeNames.size();
                }

                public boolean isEmpty() {
                        return attributeNames.isEmpty();
                }

                public boolean containsKey(Object key) {
                        return attributeNames.contains(key);
                }

                public boolean containsValue(Object value) {
                        return false;
                }

                public Object get(Object key) {
                        return ApplicationCache.getInstance().get(appId, key.toString());
                }

                public Object put(Object key, Object value) {
                        //return attributeNames.put(key.toString(), value);
                        ApplicationCache.getInstance().put(appId, key.toString(), value);
                        CacheKeyManager.setAppKey(appName, key.toString());
                        return null;
                }

                public Object remove(Object key) {
                        ApplicationCache.getInstance().remove(appId, key.toString());
                        CacheKeyManager.removeAppKey(appName, key.toString());
                        return null;
                }

                public void putAll(Map m) {

                }

                public void clear() {

                }

                public Set keySet() {
                        return new HashSet(attributeNames);
                }

                public Collection values() {
                        return null;
                }

                public Set entrySet() {
                        return null;
                }
        }
}
