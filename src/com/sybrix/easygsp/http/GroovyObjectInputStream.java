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

package com.sybrix.easygsp.http;

import java.net.URLClassLoader;
import java.io.InputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.ObjectStreamClass;

/**
 * GroovyObjectInputStream <br/>
 *
 * @author David Lee
 */
public class GroovyObjectInputStream extends java.io.ObjectInputStream {
        private ClassLoader urlClassLoader = null;

        public GroovyObjectInputStream(ClassLoader newLoader, InputStream theStream) throws IOException, StreamCorruptedException {
                super(theStream);
                urlClassLoader = newLoader;
        }

        protected Class resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {

                Class theClass = Class.forName(osc.getName(), true, urlClassLoader);

                return theClass;
        }
}
