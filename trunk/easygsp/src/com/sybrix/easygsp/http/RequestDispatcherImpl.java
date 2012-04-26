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
