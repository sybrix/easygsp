package com.sybrix.easygsp.http;

import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.JCS;

import java.util.logging.Logger;

/**
 * ApplicationCache <br/>
 * Description :
 */
public class ApplicationCache {
        private static final Logger log = Logger.getLogger(ApplicationCache.class.getName());

        private static ApplicationCache _instance;
        private static JCS jcsCache;

        private ApplicationCache()  {
                try {

                        jcsCache = JCS.getInstance("appCache");
                        
                } catch (CacheException e) {
                        throw new RuntimeException(e.getMessage(), e);
                }
        }

        public static ApplicationCache getInstance() {
                if (_instance == null) {
                        _instance = new ApplicationCache();
                }

                return _instance;
        }

        public Object get(String appId, String key) {
                return jcsCache.get(appId + "_" + key);
        }

        public void put(String appId, String key, Object value) {
                try {
                        jcsCache.put(appId + "_" + key, value);
                } catch (CacheException e) {
                        throw new RuntimeException(e.getMessage(), e);
                }

        }

        public void remove(String appId, String key) {
                try {
                        jcsCache.remove(appId + "_" + key);
                } catch (CacheException e) {
                        throw new RuntimeException(e.getMessage(), e);
                }

        }

}
