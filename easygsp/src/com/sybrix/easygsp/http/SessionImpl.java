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

import com.sybrix.easygsp.exception.NotImplementedException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

import groovy.util.ScriptException;
import groovy.util.ResourceException;
import groovy.lang.GroovyObject;

/**
 * Session <br/>
 * Description :
 */
public class SessionImpl implements HttpSession {
        private static final Logger log = Logger.getLogger(SessionImpl.class.getName());

        private static Random random = new Random(System.currentTimeMillis());
        private String sessionId;
        private long creationTime;
        private long lastAccessedTime;
        private ServletContextImpl application;
        private int maxInactiveInterval;

        private List<String> attributeNames;
        private SessionMap sessionMap;

        public SessionImpl(ServletContextImpl application, int maxInactiveInterval) {
                sessionId = createSessionId();
                creationTime = System.currentTimeMillis();
                lastAccessedTime = System.currentTimeMillis();
                this.application = application;
                this.maxInactiveInterval = maxInactiveInterval;
                attributeNames = new ArrayList();
                sessionMap = new SessionMap();
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
                        if (numberCount > 15)
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

        public void updateLastAccessedTime() {
                lastAccessedTime = System.currentTimeMillis();
        }

        public long getLastAccessedTime() {
                return lastAccessedTime;
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
                return SessionCache.getInstance().get(sessionId, s);
        }

        public Object getValue(String s) {
                return null;
        }

        public Enumeration getAttributeNames() {
                return Collections.enumeration(attributeNames);
        }


        public String[] getValueNames() {
                return new String[0];
        }

        public void setAttribute(String s, Object o) {
                SessionCache.getInstance().put(sessionId, s, o);
                attributeNames.add(s);
        }

        public void putValue(String s, Object o) {
                throw new NotImplementedException("Session.putValue() is not implemented");
        }

        public void removeAttribute(String s) {
                SessionCache.getInstance().remove(sessionId, s);
                attributeNames.add(s);
        }

        public void removeValue(String s) {
                throw new NotImplementedException("Session.removeValue() is not implemented");
        }

        public synchronized void invalidate() {
                for (String attribute : attributeNames) {
                        SessionCache.getInstance().remove(sessionId, attribute);
                }
                attributeNames.clear();
                application.getSessions().remove(sessionId);
        }

        public boolean isNew() {
                return false;
        }

        public SessionMap getSessionMap() {
                return sessionMap;
        }

        public void invokeSessionStartScript() {
                try {
                        if (application.groovyWebFileExists()) {
                                GSE3 gse = application.getGroovyScriptEngine();
                                Class clazz = gse.loadScriptByName("WEB-INF.web");
                                GroovyObject o = (GroovyObject) clazz.newInstance();
                                o.invokeMethod("onSessionStart", new Object[]{null});
                        }
                } catch (ScriptException e) {
                       log.log(SEVERE, "Session.invokeSessionStartScript() failed. message:" + e.getMessage(), e);
                } catch (ResourceException e) {
                       log.log(SEVERE, "Session.invokeSessionStartScript() failed. message:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                        log.log(SEVERE, "Session.invokeSessionStartScript() failed. message:" + e.getMessage(), e);
                } catch (InstantiationException e) {
                        log.log(SEVERE, "Session.invokeSessionStartScript() failed. message:" + e.getMessage(), e);
                }
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

        class SessionMap implements Map{
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
                      return SessionCache.getInstance().get(sessionId, key.toString());
              }

              public Object put(Object key, Object value) {
                      //return attributeNames.put(key.toString(), value);
                      SessionCache.getInstance().put(sessionId, key.toString(),value);
                      return null;
              }

              public Object remove(Object key) {
                      SessionCache.getInstance().remove(sessionId, key.toString());
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
