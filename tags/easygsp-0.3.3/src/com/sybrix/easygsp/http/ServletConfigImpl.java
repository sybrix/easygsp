package com.sybrix.easygsp.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * ServletConfigImpl <br/>
 * Description :
 */
public class ServletConfigImpl implements ServletConfig {
        private ServletContext application;
        private Map<String, String> params = new HashMap();

        public ServletConfigImpl(ServletContext s, Map params) {
                application = s;

        }

        public String getServletName() {
                return ((ServletContextImpl) application).getAppName();
        }

        public ServletContext getServletContext() {
                return application;
        }

        public String getInitParameter(String s) {
                return params.get(s);
        }

        public Enumeration getInitParameterNames() {
                return Collections.enumeration(params.keySet());
        }
}
