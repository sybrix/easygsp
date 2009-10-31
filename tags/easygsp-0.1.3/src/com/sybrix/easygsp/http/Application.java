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


import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Set;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.File;

import com.sybrix.easygsp.exception.NotImplementedException;
import com.sybrix.easygsp.util.MD5;
import com.sybrix.easygsp.http.TemplateServlet;
import com.sybrix.easygsp.server.EasyGServer;
import com.sybrix.easygsp.http.StaticControllerMethods;

/**
 * Application <br/>
 * Description :
 */
public class Application implements ServletContext {
        private static final Logger log = Logger.getLogger(Application.class.getName());

        private String appPath;
        private GSE3 groovyScriptEngine;

        private TemplateServlet templateServlet;
        private boolean hasWebGroovy = false;
        private String appId;
        private volatile boolean started;
        private List<String> attributeNames;
        private Map<String, SessionImpl> sessions;
        private String groovyFilePath;
        private File appFile;
        private String appName;
        private AttributeMap attributeMap;
        private long webGroovyLastModified;
        private ConcurrentHashMap resourceBundles;
        private List<String> errorFiles;
        private boolean is_virtual;

        public Application(File dir, boolean isVirtual) {
                //this.appId = MD5.hash(dir);
                this.is_virtual = isVirtual;
                this.appFile = dir;
                this.appId = MD5.hash(dir.getAbsolutePath());
                this.appPath = dir.getAbsolutePath();
                attributeNames = new ArrayList();
                groovyFilePath = appPath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "web.groovy";
                File _groovyFile = new File(groovyFilePath);
                hasWebGroovy = _groovyFile.exists();
                if (hasWebGroovy)
                        webGroovyLastModified = _groovyFile.lastModified();

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

        protected GSE3 getGroovyScriptEngine() {
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

        public boolean groovyFileUpdated() {
                File f = new File(groovyFilePath);
                if (f.exists()) {
                        if (f.lastModified() != webGroovyLastModified) {
                                webGroovyLastModified = f.lastModified();
                                return true;
                        }
                }

                return false;
        }

        public void setAttribute(String key, Object value) {
                ApplicationCache.getInstance().put(appId, key, value);
                attributeNames.add(key);
        }

        public void removeAttribute(String key) {
                ApplicationCache.getInstance().remove(appId, key);
                attributeNames.remove(key);
        }

        public String getServletContextName() {
                return null;
        }

        protected synchronized void startApplication() {
                if (started)
                        return;

                try {
                        //                                                groovyClassLoader = new GroovyClassLoader();
                        //                                                groovyClassLoader.addClasspath(appPath + "/WEB-INF");
                        //                                                invokeWebMethod("onApplicationStart", this);

                        loadErrorFiles();
                        AppClassLoader parentClassLoader = new AppClassLoader(new URL[]{}, this.getClass().getClassLoader());
                        parentClassLoader.setAllowThreads(EasyGServer.propertiesFile.getBoolean("allow.threads"));

                        //                                                Class gse = parentClassLoader.loadClass("groovy.util.GroovyScriptEngine");
                        //                                                Constructor con = gse.getConstructor(String[].class, ClassLoader.class);
                        //                                                groovyScriptEngine = (GSE3)con.newInstance(new String[]{appPath, appPath + System.getProperty("file.separator") + "WEB-INF"}, parentClassLoader);
                        groovyScriptEngine = new GSE3(new String[]{appPath, appPath + File.separator + "WEB-INF"}, parentClassLoader);
                        templateServlet = new TemplateServlet(groovyScriptEngine);
                        log.fine("invoking onApplicationStart for " + appName);
                        //groovyScriptEngine = new GroovyScriptEngine(new String[]{appPath, appPath + System.getProperty("file.separator") + "WEB-INF"});
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

        protected void invokeWebMethod(String method, Object... param) throws ClassNotFoundException,
                InstantiationException, IllegalAccessException, ScriptException, ResourceException {

                Class clazz = groovyScriptEngine.loadScriptByName("web.groovy");

                GroovyObject o = (GroovyObject) clazz.newInstance();
                o.invokeMethod(method, param);

                //                Class cls = groovyClassLoader.loadClass("web");
                //                GroovyObject go = (GroovyObject)cls.newInstance();
                //                go.invokeMethod(method, param);
        }

        protected void killApp() {
                for (String attributeName : attributeNames) {
                        ApplicationCache.getInstance().remove(appId, attributeName);
                }

                Object keys[] = sessions.keySet().toArray();
                for (Object key : keys) {
                        sessions.get(key.toString()).invalidate();
                        sessions.remove(key.toString());
                }
        }

        // should this hidden >
        public void stopApplication() {
                try {
                        if (hasWebGroovy){
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

        public boolean isVirtualHost() {
                return is_virtual;
        }

        protected void setVirtualHosted(boolean is_virtual) {
                this.is_virtual = is_virtual;
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
                        return null;
                }

                public Object remove(Object key) {
                        ApplicationCache.getInstance().remove(appId, key.toString());
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
