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

import com.sybrix.easygsp.exception.NotImplementedException;
import com.sybrix.easygsp.server.EasyGServer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

import groovy.util.GroovyScriptEngine;
import groovy.util.ScriptException;
import groovy.util.ResourceException;
import groovy.lang.GroovyObject;

/**
 * Session <br/>
 * Description :
 */
@SuppressWarnings("deprecation")
public class SessionImpl implements HttpSession, Serializable {

        private static final long serialVersionUID = 1L;

        private static final Logger logger = Logger.getLogger(SessionImpl.class.getName());
        private static Random random = new Random(System.currentTimeMillis());

        private String sessionId;
        private Long creationTime;
        private Long lastAccessedTime;
        private transient ServletContextImpl application;
        private Integer maxInactiveInterval;
        private Map<String, Object> sessionAttributes;
        private Map<String, FlashMessage> flash;

        public SessionImpl(ServletContextImpl application, int maxInactiveInterval) {
                this.application = application;
                this.maxInactiveInterval = maxInactiveInterval;

                sessionId = createSessionId();
                creationTime = System.currentTimeMillis();
                lastAccessedTime = System.currentTimeMillis();
                sessionAttributes = new HashMap<String, Object>();
                flash = new FlashMap();
        }

        public SessionImpl(ServletContextImpl application, String sessionId, int maxInactiveInterval) {
                this(application, maxInactiveInterval);
                this.sessionId = sessionId;
        }

        private String createSessionId() {
                StringBuffer sb = new StringBuffer();
                int c = 0;
                int numberCount = 0;
                while (true) {
                        c = random.nextInt(42) + 48;
                        if (c >= 58 && c <= 64)
                                continue;
                        sb.append((char) c);
                        if (numberCount > 20)
                                break;
                        numberCount++;
                }

                return sb.toString();
        }

        public long getCreationTime() {
                return creationTime;
        }

        public String getId() {
                return sessionId;
        }

        public void setLastAccessTime(Long lastAccessTime) {
                if (lastAccessTime > this.lastAccessedTime)
                        this.lastAccessedTime = lastAccessTime;
        }

        public void updateLastAccessedTime() {
                lastAccessedTime = System.currentTimeMillis();

//                SessionMessage sessionMessage = new SessionMessage(application.getAppName(), application.getAppPath(), sessionId, "setLastAccessTime", new Object[]{lastAccessedTime});
//                EasyGServer.sendToChannel(sessionMessage);

        }

        public long getLastAccessedTime() {
                return lastAccessedTime;
        }

        public ServletContext getApp() {
                return application;
        }

        public void setApp(ServletContext o) {

        }

        public ServletContext getServletContext() {
                return application;
        }

        public ServletContext getApplication() {
                return application;
        }

        public void setMaxInactiveInterval(int maxInactiveInterval) {
                this.maxInactiveInterval = maxInactiveInterval;
        }

        public int getMaxInactiveInterval() {
                return maxInactiveInterval;
        }

        public HttpSessionContext getSessionContext() {
                throw new NotImplementedException("Session.getSessionContext() is not implemented");
        }

        public Object getAttribute(String s) {
                return sessionAttributes.get(s);
        }

        public Object getValue(String s) {
                return null;
        }

        public Enumeration getAttributeNames() {
                return Collections.enumeration(sessionAttributes.keySet());
        }

        public String[] getValueNames() {
                return new String[0];
        }

        public void setAttribute(String key, Object value) {
                if (value != null) {
                        sessionAttributes.put(key, value);

                } else
                        sessionAttributes.put(key, null);

//                SessionMessage sessionMessage = new SessionMessage(application.getAppName(), application.getAppPath(), sessionId, "remote_setAttribute", new Object[]{key, value});
//                EasyGServer.sendToChannel(sessionMessage);
        }

        public void remote_setAttribute(String key, Object value) {
                sessionAttributes.put(key, value.toString());
        }

        public void putValue(String s, Object o) {
                throw new NotImplementedException("Session.putValue() is not implemented");
        }

        public void removeAttribute(String key) {
                sessionAttributes.remove(key);
        }

        public final void setApplication(ServletContextImpl application) {
                this.application = application;
        }

        public void removeValue(String s) {
                throw new NotImplementedException("Session.removeValue() is not implemented");
        }

        public synchronized void invalidate() {

                try {
                        if (RequestThreadInfo.get().getApplication().groovyWebFileExists())
                                RequestThreadInfo.get().getApplication().invokeWebMethod("onSessionEnd", new Object[]{this});
                } catch (Exception e) {
                        //e.printStackTrace();                                                                                                        
                }

                sessionAttributes.clear();
                application.getSessions().remove(sessionId);
                sessionId = null;

                if (RequestThreadInfo.get() != null)
                        if (RequestThreadInfo.get().getRequestImpl() != null)
                                RequestThreadInfo.get().getRequestImpl().nullSession();

                ClusterMessage sessionMessage = new ClusterMessage(application.getAppName(), application.getAppPath(), sessionId, "remote_invalidate", new Object[]{});
                EasyGServer.sendToChannel(sessionMessage);
        }

        public synchronized void remote_invalidate() {
                sessionAttributes.clear();
                application.getSessions().remove(sessionId);
        }

        public boolean isNew() {
                return false;
        }

//        public SessionMap getSessionMap() {
//                return sessionMap;
//        }

        public void invokeSessionStartScript() {
                try {
                        if (application.groovyWebFileExists()) {
                                GroovyScriptEngine gse = application.getGroovyScriptEngine();
                                Class clazz = gse.loadScriptByName("WEB-INF.web");
                                GroovyObject o = (GroovyObject) clazz.newInstance();
                                o.invokeMethod("onSessionStart", new Object[]{null});
                        }
                } catch (ScriptException e) {
                        logger.log(SEVERE, "Session.invokeSessionStartScript() failed. message:" + e.getMessage(), e);
                } catch (ResourceException e) {
                        logger.log(SEVERE, "Session.invokeSessionStartScript() failed. message:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                        logger.log(SEVERE, "Session.invokeSessionStartScript() failed. message:" + e.getMessage(), e);
                } catch (InstantiationException e) {
                        logger.log(SEVERE, "Session.invokeSessionStartScript() failed. message:" + e.getMessage(), e);
                }
        }

        public Map<String, FlashMessage> getFlash() {
                return flash;
        }

        public void setFlash(Map<String, FlashMessage> flash) {
                this.flash = flash;
        }

        @Override
        public String toString() {
                return "sessionid=" + sessionId + ", " + super.toString();
        }

        //        public void invokeSessionStopScript() {
//                        try {
//                                if (application.groovyWebFileExists()) {
//                                        GSE3 gse = application.getGroovyScriptEngine();
//                                        Class clazz = gse.loadScriptByName("WEB-INF.web");
//                                        GroovyObject o = (GroovyObject) clazz.newInstance();
//                                        o.invokeMethod("onSessionEnd", new Object[]{null});
//                                }
//                        } catch (ScriptException e) {
//                               log.log(Level.FINE, "Session.invokeSessionStopScript() failed. message:" + e.getMessage(), e);
//                        } catch (ResourceException e) {
//                               log.log(Level.FINE, "Session.invokeSessionStopScript() failed. message:" + e.getMessage(), e);
//                        } catch (IllegalAccessException e) {
//                                log.log(Level.FINE, "Session.invokeSessionStopScript() failed. message:" + e.getMessage(), e);
//                        } catch (InstantiationException e) {
//                                log.log(Level.FINE, "Session.invokeSessionStopScript() failed. message:" + e.getMessage(), e);
//                        } catch (Throwable e) {
//                                log.log(Level.FINE, "Session.invokeSessionStopScript() failed. message:" + e.getMessage(), e);
//                        }
//
//        }

//        class SessionMap implements Map {
//                public int size() {
//                        return attributeNames.size();
//                }
//
//                public boolean isEmpty() {
//                        return attributeNames.isEmpty();
//                }
//
//                public boolean containsKey(Object key) {
//                        return attributeNames.contains(key);
//                }
//
//                public boolean containsValue(Object value) {
//                        return false;
//                }
//
//                public Object get(Object key) {
//                        return SessionCache.getInstance().get(sessionId, key.toString());
//                }
//
//                public Object put(Object key, Object value) {
//                        //return attributeNames.put(key.toString(), value);
//                        SessionCache.getInstance().put(application.getAppName(), sessionId, key.toString(), value);
//                        return null;
//                }
//
//                public Object remove(Object key) {
//                        SessionCache.getInstance().remove(application.getAppName(), sessionId, key.toString());
//                        return null;
//                }
//
//                public void putAll(Map m) {
//
//                }
//
//                public void clear() {
//
//                }
//
//                public Set keySet() {
//                        return new HashSet(attributeNames);
//                }
//
//                public Collection values() {
//                        return null;
//                }
//
//                public Set entrySet() {
//                        return null;
//                }
//        }
}
