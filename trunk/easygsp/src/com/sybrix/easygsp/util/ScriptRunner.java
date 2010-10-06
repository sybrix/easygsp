package com.sybrix.easygsp.util;

import groovy.lang.Script;
import com.sybrix.easygsp.http.GSE5;

/**
 * ScriptRunner <br/>
 *
 * @author David Lee
 */
public class ScriptRunner extends Script {
        private Class controllerClass;
        public ScriptRunner(GSE5 gse, String clazz){
                try {
                        controllerClass = gse.getGroovyClassLoader().loadClass(clazz);
                } catch (ClassNotFoundException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
        }

        public Object run() {
                //Framework.processPage(controllerClass);
                return null;
        }
}
