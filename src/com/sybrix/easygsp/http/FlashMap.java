package com.sybrix.easygsp.http;

import java.util.HashMap;

/**
 * FlashMap <br/>
 *
 * @author David Lee
 */
public class FlashMap extends HashMap {
        @Override
        public Object put(Object key, Object value) {
                return super.put(key, new FlashMessage(value.toString()));    //To change body of overridden methods use File | Settings | File Templates.
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
