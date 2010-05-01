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
