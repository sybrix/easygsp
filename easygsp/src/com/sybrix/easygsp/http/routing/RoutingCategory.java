package com.sybrix.easygsp.http.routing;

import com.sybrix.easygsp.http.ServletContextImpl;
import groovy.lang.Binding;
import groovy.lang.GroovyObject;



public class RoutingCategory {

        public static void add(GroovyObject self, String method, String path, String controller) {
                Binding binding = (Binding) self.getProperty("binding");
                ServletContextImpl app = (ServletContextImpl)binding.getProperty("app");

                app.getRouter().addRoute(method,path,controller);
        }
}
