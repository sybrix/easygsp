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
