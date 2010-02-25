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

import java.net.URLClassLoader;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.security.PermissionCollection;
import java.security.CodeSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.util.Enumeration;

/**
 * AppClassLoader <br/>
 * Description :
 */
public class AppClassLoader extends URLClassLoader {


        private boolean allowThreads;

        public boolean isAllowThreads() {
                return allowThreads;
        }

        public void setAllowThreads(boolean allowThreads) {
                this.allowThreads = allowThreads;
        }

        public AppClassLoader(URL[] urls) {
                super(urls);
        }

        public AppClassLoader(URL[] urls, ClassLoader parent) {
                super(urls, parent);
        }

        public AppClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
                super(urls, parent, factory);
        }

        protected PermissionCollection getPermissions(CodeSource codesource) {
                return super.getPermissions(codesource);
        }

        protected void addURL(URL url) {
                super.addURL(url);
        }

        protected Package definePackage(String name, Manifest man, URL url) throws IllegalArgumentException {
                return super.definePackage(name, man, url);
        }

        protected Class<?> findClass(String name) throws ClassNotFoundException {


                return super.findClass(name);
        }

        public URL findResource(String name) {
                return super.findResource(name);
        }

        public Enumeration<URL> findResources(String name) throws IOException {
                return super.findResources(name);
        }

        public URL[] getURLs() {
                return super.getURLs();
        }

        public URL getResource(String name) {
                return super.getResource(name);
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals("java.lang.Thread") || name.startsWith("com.sybrix.easygsp") || name.startsWith("org.apache.jcs"))
                        throw new ClassNotFoundException(name);
                return super.loadClass(name);
        }

        public InputStream getResourceAsStream(String name) {
                return super.getResourceAsStream(name);
        }

        public Enumeration<URL> getResources(String name) throws IOException {
                return super.getResources(name);
        }

        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                ServletContextImpl path = RequestThreadInfo.get().getApplication();
                if (path != null) {
                        if ((name.equals("java.lang.Thread") && allowThreads == false)
                                || name.startsWith("com.sybrix.easygsp") || name.startsWith("org.apache.jcs")) {
                                throw new ClassNotFoundException(name);
                        }
                }
                return super.loadClass(name, resolve);
        }
}
