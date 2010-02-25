package com.sybrix.easygsp.util;

import java.util.HashMap;

/**
 * CaseInsensitiveMap <br/>
 *                                                                           
 * @author David Lee
 */
public class CaseInsensitiveMap extends HashMap {
        @Override
        public Object get(Object key) {
                return super.get(key.toString().toUpperCase());
        }

        @Override
        public Object put(Object key, Object value) {
                return super.put(key.toString().toUpperCase(), value);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public Object remove(Object key) {
                return super.remove(key.toString().toUpperCase());    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public boolean containsValue(Object value) {
                return super.containsValue(value.toString().toUpperCase());    //To change body of overridden methods use File | Settings | File Templates.
        }
}
