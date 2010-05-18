package com.sybrix.easygsp.http;

import java.util.HashMap;

/**
 * FlashMap <br/>
 *
 * @author David Lee
 */
public class FlashMap extends HashMap {
        private static final long serialVersionUID = 1L;
        
        @Override
        public Object put(Object key, Object value) {
                return super.put(key, new FlashMessage(value.toString()));
        }

        @Override
        public Object get(Object key) {
                Object val = super.get(key);
                if (val == null)
                        return "";
                else
                        return val;
        }
}
