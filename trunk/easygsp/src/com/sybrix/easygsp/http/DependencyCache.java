package com.sybrix.easygsp.http;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DependencyCache <br/>
 *
 * @author David Lee
 */
public class DependencyCache extends HashMap {
        @Override
        public Object get(Object key) {
                if (!containsKey(key)){
                        put(key, new ArrayList());
                }

                return super.get(key);
        }
}
