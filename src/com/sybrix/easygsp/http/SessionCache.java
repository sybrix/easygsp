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

import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.JCS;

import java.util.logging.Logger;

/**
 * ApplicationCache <br/>
 * Description :
 */
public class SessionCache {
        private static final Logger log = Logger.getLogger(SessionCache.class.getName());

        private static SessionCache _instance;
        private static JCS jcsCache;

        private SessionCache() {
                try {
                        

                        jcsCache = JCS.getInstance("sessionCache");
                } catch (CacheException e) {
                        throw new RuntimeException(e);
                }
        }

        public static SessionCache getInstance() {
                if (_instance == null) {
                        _instance = new SessionCache();
                }

                return _instance;
        }

        public Object get(String session_id, String key) {
                return jcsCache.get(session_id + "_" + key);
        }

        public void put(String session_id, String key, Object value) {
                try {
                        jcsCache.put(session_id + "_" + key, value);
                } catch (CacheException e) {
                        throw new RuntimeException(e.getMessage(), e);
                }
        }

        public void remove(String session_id, String key) {
                try {
                        jcsCache.remove(session_id + "_" + key);
                } catch (CacheException e) {
                        throw new RuntimeException(e.getMessage(), e);
                }

        }

}
