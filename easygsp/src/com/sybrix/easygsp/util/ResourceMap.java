package com.sybrix.easygsp.util;

import java.util.*;


public class ResourceMap extends HashMap{
        ResourceBundle bundle;

        public ResourceMap(ResourceBundle bundle) {
                this.bundle = bundle;
        }

        @Override
        public Object get(Object o) {
                return bundle.getString(o.toString());
        }

        public Locale getLocale(){
                return bundle.getLocale();
        }

        public Set<String> getKeySet(){
                  return bundle.keySet();
        }

        @Override
        public boolean containsKey(Object o) {
                return bundle.containsKey(o.toString());
        }
}
