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

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

/**
 * BundleClassLoader <br/>
 * Description :
 */
public class BundleClassLoader extends ClassLoader{
        String appPath;
        protected BundleClassLoader(String appPath, ClassLoader parent) {
                super(parent);
                this.appPath = appPath;
        }

        protected URL findResource(String name) {
                File f = new File(appPath + File.separator+  "WEB-INF" + File.separator + "i18n" + File.separator + name);
                if (f.exists()){
                        try {
                                return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                                return null;
                        }
                }
                return super.findResource(name);
        }
}
