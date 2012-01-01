package com.sybrix.easygsp.http;

import com.sybrix.easygsp.exception.NotImplementedException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * RequestDispatcher <br/>
 * Description :
 */
public class RequestDispatcherImpl implements RequestDispatcher {
        private String file;
        
        public RequestDispatcherImpl(String file) {
                this.file = file;
        }

        public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {

                HttpServletRequest request = (HttpServletRequest)servletRequest;
                HttpServletResponse response = (HttpServletResponse)servletResponse;
                CustomServletBinding servletBinding = ((RequestImpl)request).getServletBinding();
                ServletContextImpl application = (ServletContextImpl)servletBinding.getVariable("application");

                RequestThread.processScriptController(file, application.getGroovyScriptEngine(), servletBinding);

        }

        public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
                throw new NotImplementedException("RequestDispatcherImpl.include() is not implemented");
        }
}
