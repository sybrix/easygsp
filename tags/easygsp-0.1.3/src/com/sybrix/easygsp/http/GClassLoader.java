package com.sybrix.easygsp.http;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * GClassLoader <br/>
 *
 * @author David Lee
 */
public class GClassLoader extends GroovyClassLoader{

        public GClassLoader(ClassLoader loader) {
                super(loader);    //To change body of overridden methods use File | Settings | File Templates.
        }

        public GClassLoader(GroovyClassLoader parent) {
                super(parent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        public GClassLoader(ClassLoader parent, CompilerConfiguration config, boolean useConfigurationClasspath) {
                super(parent, config, useConfigurationClasspath);    //To change body of overridden methods use File | Settings | File Templates.
        }

        public GClassLoader(ClassLoader loader, CompilerConfiguration config) {
                super(loader, config);    //To change body of overridden methods use File | Settings | File Templates.
        }


       public Class parseClass(final InputStream in, final String fileName, final String appPath) throws CompilationFailedException {
        // For generic input streams, provide a catch-all codebase of
        // GroovyScript
        // Security for these classes can be administered via policy grants with
        // a codebase of file:groovy.script
        GroovyCodeSource gcs = (GroovyCodeSource) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new GroovyCodeSource(in, fileName, appPath);
            }
        });
        return parseClass(gcs);
    }
}
