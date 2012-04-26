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
import com.sybrix.easygsp.http.routing.UrlParameter;
import com.sybrix.easygsp.server.EasyGServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletInputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Locale;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.security.Principal;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.File;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import groovy.lang.Binding;


/**
 * Custom implementation of HttpServletRequest <br/>
 */
public class RequestImpl implements HttpServletRequest {
        private static final Logger logger = Logger.getLogger(RequestImpl.class.getName());

        private Map<String, String> headers;
        private Map<String, Object> queryStringParameters;
        private Map<String, Object> formParameters;
        private Map<String, Object> allParameters;
        private Map<String, Object> requestAttributes;
        private boolean queryStringParsed = false;
        private BufferedReader bufferedReader;
        private InputStream ios;
        private ServletInputStreamImpl myServletInputStream;
        private Map<String, Cookie> cookies;
        private ServletContextImpl application;
        private CustomServletBinding servletBinding;
        private boolean cookiesParsed;
        private SessionImpl session;
        private ResponseImpl response;
        private static String altGroovyExtension;
        private static String templateExtension;
        private static String viewExtension;
        private static String tempUploadDirectory;
        private static Long maxUploadSize;
        private static int uploadThresholdSize;
        private Map uploads;
        protected boolean isMulitpart = false;
        private StringBuffer requestURL;

        static {
                templateExtension = EasyGServer.propertiesFile.getString("template.extension", ".gsp");
                altGroovyExtension = EasyGServer.propertiesFile.getString("alt.groovy.extension", ".gspx");
                viewExtension = EasyGServer.propertiesFile.getString("view.extension", ".gspx");
                tempUploadDirectory = EasyGServer.propertiesFile.getString("file.upload.temp.directory", System.getProperty("java.io.tmpdir"));
                maxUploadSize = EasyGServer.propertiesFile.getLong("file.upload.max.filesize", -1L);
                uploadThresholdSize = EasyGServer.propertiesFile.getInt("file.upload.threshold", 2621440);
        }

        public RequestImpl(InputStream ios, Map<String, String> headers, ServletContextImpl application, ResponseImpl response) throws FileUploadException {


                this.ios = ios;
                this.headers = headers;
                allParameters = new HashMap();
                requestAttributes = new HashMap();
                isMulitpart = true;

                if (!ServletFileUpload.isMultipartContent(this)) {
                        parseParameters();
                        parseFormParameters();
                        isMulitpart = false;
                        setAttribute("isMultipart", false);
                }

                this.application = application;
                this.response = response;

        }

        public String getAuthType() {
                throw new NotImplementedException("Request.getAuthType() is not implemented");
        }

        public Cookie[] getCookies() {
                if (cookies == null)
                        parseCookies();

                Cookie[] cookieAry = new Cookie[cookies.values().size()];
                cookies.values().toArray(cookieAry);
                return cookieAry;
        }

        protected void removeCookie(String cookieName) {
                if (cookies == null)
                        parseCookies();


                cookies.remove(cookieName);
        }

        public Cookie getCookie(String cookieName) {
                if (cookies == null)
                        parseCookies();

                return cookies.get(cookieName);
        }

        public long getDateHeader(String s) {
                throw new NotImplementedException("Request.getPathTranslated() is not implemented");
        }

        public String getHeader(String header) {
                return headers.get(header);
        }

        public Enumeration getHeaders(String s) {
                return Collections.enumeration(headers.keySet());
        }

        public Enumeration getHeaderNames() {
                return Collections.enumeration(headers.keySet());
        }

        public int getIntHeader(String s) throws NumberFormatException {
                return Integer.parseInt(getHeader(s));
        }

        public String getMethod() {
                return headers.get(RequestHeaders.REQUEST_METHOD);
        }

        public String getPathInfo() {
                throw new NotImplementedException("Request.getPathInfo() is not implemented");
        }

        public String getPathTranslated() {

                throw new NotImplementedException("Request.getPathTranslated() is not implemented");
        }

        public String getContextPath() {
                throw new NotImplementedException("Request.getContextPath() is not implemented");
        }

        public String getQueryString() {
                return headers.get(RequestHeaders.QUERY_STRING);
        }

        public String getRemoteUser() {
                throw new NotImplementedException("Request.getRemoteUser() is not implemented");
        }

        public boolean isUserInRole(String s) {
                throw new NotImplementedException("Request.isUserInRole() is not implemented");
        }

        public Principal getUserPrincipal() {
                throw new NotImplementedException("Request.getUserPrincipal() is not implemented");
        }

        public String getRequestedSessionId() {
                throw new NotImplementedException("Request.getRequestedSessionId() is not implemented");
        }

        public String getRequestURI() {
                return headers.get(RequestHeaders.REQUEST_URI);
        }

        public StringBuffer getRequestURL() {
                if (requestURL == null) {
                        requestURL = new StringBuffer();
                        String uri = headers.get(RequestHeaders.REQUEST_URI);

                        int end = uri.indexOf('?');
                        int start = uri.lastIndexOf('/') + 1;

                        if (end < 0)
                                end = uri.length();

                        if (start < 0)
                                start = 0;

                        requestURL.append(uri.substring(start, end));

                }

                return requestURL;
        }

        public String getServletPath() {
                throw new NotImplementedException("Request.getServletPath() is not implemented");
        }

        public HttpSession getSession(boolean b) {
                return getHttpSession(b);
        }

        public HttpSession getSession() {
                return getHttpSession(true);
        }

        private HttpSession getHttpSession(boolean createSession) {

                if (session != null)
                        return session;

                if (!cookiesParsed)
                        parseCookies();

                Cookie requestCookie = cookies.get("GSESSIONID");
                if (requestCookie != null) {
                        session = application.getSessions().get(requestCookie.getValue());
                }

                if (session != null)
                        return session;

                if (session == null && createSession) {
                        logger.log(Level.FINE, "creating session...");
                        session = new SessionImpl(application, EasyGServer.propertiesFile.getInt("session.timeout", 15));
                        servletBinding.setVariable("session", session);

                        RequestThreadInfo.get().getBinding().setVariable("flash", session.getFlash());

                        application.getSessions().put(session.getId(), session);
                        Cookie cookie = new Cookie("GSESSIONID", session.getId());

                        //cookie.setDomain(headers.get("SERVER_NAME"));
                        cookie.setPath("/");

                        response.addCookie(cookie);

                        try {
                                if (application.groovyWebFileExists()) {
                                        application.invokeWebMethod("onSessionStart", new Object[]{session});
                                }

                                logger.log(Level.FINE, "session \"" + session.getId() + "\" created");
                        } catch (Exception e) {
                                logger.log(SEVERE, "onSessionStart failed.", e);
                        }
                }

                return session;
        }

        public boolean isRequestedSessionIdValid() {
                throw new NotImplementedException("Request.isRequestedSessionIdValid() is not implemented");
        }

        public boolean isRequestedSessionIdFromCookie() {
                throw new NotImplementedException("Request.isRequestedSessionIdFromCookie() is not implemented");
        }

        public boolean isRequestedSessionIdFromURL() {
                throw new NotImplementedException("Request.isRequestedSessionIdFromURL() is not implemented");
        }

        public boolean isRequestedSessionIdFromUrl() {
                throw new NotImplementedException("Request.isRequestedSessionIdFromUrl() is not implemented");
        }

        public Object getAttribute(String attributeName) {
                return requestAttributes.get(attributeName);
        }

        public Enumeration getAttributeNames() {
                return Collections.enumeration(requestAttributes.keySet());
        }

        protected Map getAttributes() {
                return requestAttributes;
        }

        public String getCharacterEncoding() {
                return null;
                //throw new NotImplementedException("Request.getCharacterEncoding() is not implemented");
        }

        public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
                throw new NotImplementedException("Request.setCharacterEncoding() is not implemented");
        }

        public int getContentLength() {
                try {
                        return getIntHeader(RequestHeaders.CONTENT_LENGTH);
                } catch (Exception e) {
                        return -1;
                }
        }

        public String getContentType() {
                return headers.get(RequestHeaders.CONTENT_TYPE);
        }

        public ServletInputStream getInputStream() throws IOException {
                if (myServletInputStream == null) {
                        myServletInputStream = new ServletInputStreamImpl(ios);
                }

                return myServletInputStream;
        }

        public String getParameter(String parameterName) {
                Object obj = allParameters.get(parameterName);
                if (obj == null)
                        return null;

                if (obj instanceof String) {
                        return (String) obj;
                } else {
                        String[] parameters = (String[]) obj;
                        return (String) parameters[0];
                }
        }

        public Enumeration getParameterNames() {
                return Collections.enumeration(allParameters.keySet());
        }

        public String[] getParameterValues(String parameterName) {
                Object obj = allParameters.get(parameterName);

                if (obj == null) {
                        return new String[]{""};
                }
                if (obj instanceof String) {
                        return new String[]{obj.toString()};
                } else if (obj instanceof String[]) {
                        return (String[]) obj;
                } else {
                        List parameters = (List) obj;
                        String aryVal[] = new String[parameters.size()];
                        parameters.toArray(aryVal);
                        return aryVal;
                }
        }

        public Map getParameterMap() {
                return allParameters;
        }

        public String getProtocol() {
                return headers.get(RequestHeaders.SERVER_PROTOCOL);
        }

        public String getScheme() {
                throw new NotImplementedException("Request.getLocale() is not implemented");
        }

        public String getServerName() {
                return parseHostName(headers.get(RequestHeaders.SERVER_NAME));
        }

        private String parseHostName(String name) {
                try {
                        if (name.indexOf(':') > -1)
                                return name.substring(0, name.length() - 5);

                        return name;
                } catch (Exception e) {
                        return null;
                }
        }

        public int getServerPort() {
                return Integer.parseInt(headers.get(RequestHeaders.SERVER_PORT));
        }

        public BufferedReader getReader() throws IOException {
                if (bufferedReader == null) {
                        bufferedReader = new BufferedReader(new InputStreamReader(ios));
                }

                return bufferedReader;
        }

        public String getRemoteAddr() {
                return headers.get(RequestHeaders.REMOTE_ADDR);
        }

        public String getRemoteHost() {
                return headers.get(RequestHeaders.REMOTE_ADDR);
        }

        public void setAttribute(String attributeName, Object object) {
                requestAttributes.put(attributeName, object);
        }

        public void removeAttribute(String attributeName) {
                requestAttributes.remove(attributeName);
        }

        public Locale getLocale() {
                throw new NotImplementedException("Request.getLocale() is not implemented");
        }

        public Enumeration getLocales() {
                throw new NotImplementedException("Request.getLocales() is not implemented");
        }

        public boolean isSecure() {
                String ssl = headers.get("HTTPS");
                if (ssl != null && ssl.equals("on")) {
                        return true;
                }

                return false;
        }

        public RequestDispatcher getRequestDispatcher(String file) {
                return new RequestDispatcherImpl(file);
        }

        public String getRealPath(String s) {
                throw new NotImplementedException("Request.getLocale() is not implemented");
        }

        public int getRemotePort() {
                return getIntHeader(RequestHeaders.REMOTE_PORT);
        }

        public String getLocalName() {
                return parseHostName(headers.get(RequestHeaders.HTTP_HOST));
        }

        public String getLocalAddr() {
                return headers.get(RequestHeaders.SERVER_ADDR);
        }

        public int getLocalPort() {
                return getIntHeader(RequestHeaders.SERVER_PORT);
        }

        private Map<String, Object> parseParameters() {
                if (allParameters == null) {
                        allParameters = new HashMap();

                }
                allParameters.putAll(parseQueryStringParameters());


                // if a key is in the querystring more than once, the values are returned in a list. 
                for (String key : allParameters.keySet()) {
                        if (allParameters.get(key) instanceof List) {
                                allParameters.put(key, getParameterValues(key));
                        }
                }
                return allParameters;
        }

        private Map<String, Object> parseFormParameters() {

                formParameters = new HashMap();
                if (getContentLength() > 0) {
                        int bufferSize = (4096 * 2);
                        byte buff[] = new byte[bufferSize];
                        int c = 0;
                        StringBuffer sb = new StringBuffer();
                        try {
                                int totalRead = 0;

                                while (true) {
                                        int amountLeft = getContentLength() - totalRead;
                                        if (amountLeft >= buff.length)
                                                amountLeft = buff.length;

                                        c = ios.read(buff, 0, amountLeft);
                                        if (c == -1)
                                                break;

                                        sb.append(new String(buff, 0, c));
                                        totalRead += c;
                                        if (totalRead >= getContentLength())
                                                break;
                                }

                                formParameters = ParsingUtil.parse(sb.toString(), "utf-8");

                        } catch (IOException e) {
                                e.printStackTrace();
                        }

                }

                allParameters.putAll(formParameters);

                for (String key : allParameters.keySet()) {
                        if (allParameters.get(key) instanceof List) {
                                allParameters.put(key, getParameterValues(key));
                        }
                }

                return allParameters;
        }

        private Map<String, Object> parseQueryStringParameters() {
                if (queryStringParsed) {
                        return queryStringParameters;
                }

                queryStringParameters = ParsingUtil.parse(headers.get(RequestHeaders.QUERY_STRING), "utf-8");
                queryStringParsed = true;

                return queryStringParameters;
        }

        class ServletInputStreamImpl extends ServletInputStream {
                private InputStream inputStream;

                public ServletInputStreamImpl(InputStream inputStream) {
                        this.inputStream = inputStream;
                }

                public int read(byte b[], int off, int len) throws IOException {
                        return inputStream.read(b, off, len);
                }

                public int read() throws IOException {
                        return inputStream.read();
                }
        }

        private void parseCookies() {
                if (cookies == null)
                        cookies = new HashMap();

                String cookieValue = getHeader(RequestHeaders.COOKIE);
                if (cookieValue != null) {
                        String pair[] = cookieValue.split(";");

                        for (int i = 0; i < pair.length; i++) {
                                String val[] = pair[i].split("=");
                                if (val.length > 1) {
                                        cookies.put(val[0].trim(), new Cookie(val[0].trim(), val[1]));
                                }
                        }
                }

                cookiesParsed = true;
        }

        public CustomServletBinding getServletBinding() {
                return servletBinding;
        }

        protected void setServletBinding(CustomServletBinding servletBinding) {
                this.servletBinding = servletBinding;
        }

        public void forward(String file) throws IOException, ServletException {
                RequestThreadInfo.get().increaseForwardCount();

                if (file.endsWith(viewExtension) || file.endsWith(templateExtension)) {
                        file = constructForwardPath(file, servletBinding);
                        logger.fine("fowarding to file: " + file);
                        //application.getTemplateServlet().service(file, this, response, servletBinding);
                        if (!RequestThreadInfo.get().getParsedRequest().getRequestURI().equals(file)) {
                                RequestThreadInfo.get().getParsedRequest().setRequestURI(file);
                                RequestThreadInfo.get().getParsedRequest().setRequestFilePath(
                                        RequestThreadInfo.get().getParsedRequest().getAppPath() + "/" + file

                                );
                                //requestURIPath = webAppDir + "/" + path;
                        }
                        application.getTemplateServlet().service(this, response);
                } else if (file.endsWith(".groovy")) {
                        file = constructForwardPath(file, servletBinding).replace(".groovy", ".gspx");

                        if (!RequestThreadInfo.get().getParsedRequest().getRequestURI().equals(file)) {
                                RequestThreadInfo.get().getParsedRequest().setRequestURI(file);
                                RequestThreadInfo.get().getParsedRequest().setRequestFilePath(
                                        RequestThreadInfo.get().getParsedRequest().getAppPath() + "/" + file

                                );
                                //requestURIPath = webAppDir + "/" + path;
                        }

                        setAttribute("_explicitForward", false);
                        RequestThread.processScriptController(file, application.getGroovyScriptEngine(), servletBinding);
                }
        }

        public void forwardToView(String file) throws IOException, ServletException {
                //application.getTemplateServlet().service(file, this, response, servletBinding);
                application.getTemplateServlet().service(this, response);
        }

        protected static String constructForwardPath(String s, Binding binding) {
                String path = RequestThreadInfo.get().getParsedRequest().getRequestURI();
                int i = 0;
                if (s.charAt(0) != '/' && (i = path.lastIndexOf('/')) > 0) {
                        s = path.substring(0, i) + "/" + s;
                }

                return s;
        }

        public List<FileItem> parseFileUploads(int threshold, long maxFileSize) throws FileUploadException {
                if (ServletFileUpload.isMultipartContent(this)) {
                        DiskFileItemFactory factory = new DiskFileItemFactory(threshold, new File(tempUploadDirectory));

                        // Create a new file upload handler
                        ServletFileUpload upload = new ServletFileUpload(factory);

                        // Set overall request size constraint
                        upload.setSizeMax(maxFileSize);

                        // Parse the request
                        List<FileItem> l = upload.parseRequest(this);
                        uploads = new HashMap();
                        for (FileItem f : l) {
                                if (f.isFormField()) {
                                        allParameters.put(f.getFieldName(), f.getString());
                                } else {
                                        if (f.getSize() > 0) {
                                                if (uploads.containsKey(f.getFieldName())) {
                                                        Object obj = uploads.get(f.getFieldName());
                                                        if (obj instanceof List) {
                                                                ((List) obj).add(f);
                                                        } else {
                                                                List newUploads = new ArrayList();
                                                                newUploads.add(obj);
                                                                uploads.put(f.getFieldName(), newUploads);
                                                        }
                                                } else
                                                        uploads.put(f.getFieldName(), f);
                                        }
                                }
                        }

                        servletBinding.populateParameters(this);

                        return l;
                } else {
                        return new ArrayList();
                }
        }

        public Map getUploads() {
                if (uploads == null)
                        return new HashMap();
                return uploads;
        }

        public List<FileItem> parseFileUploads() throws FileUploadException {

                return parseFileUploads(uploadThresholdSize, maxUploadSize);
        }

        protected void nullSession() {
                session = null;
        }

        //        public int size() {
        //                return requestAttributes.size();
        //        }
        //
        //        public boolean isEmpty() {
        //                return requestAttributes.isEmpty();
        //        }
        //
        //        public boolean containsKey(Object key) {
        //                return requestAttributes.containsKey(key);
        //        }
        //
        //        public boolean containsValue(Object value) {
        //                return requestAttributes.containsValue(value);
        //        }
        //
        //        public Object get(Object key) {
        //                return requestAttributes.get(key);
        //        }
        //
        //        public Object put(Object key, Object value) {
        //                return requestAttributes.put(key.toString(), value);
        //        }
        //
        //        public Object remove(Object key) {
        //                return requestAttributes.remove(key);
        //        }
        //
        //        public void putAll(Map m) {
        //                requestAttributes.putAll(m);
        //        }
        //
        //        public void clear() {
        //
        //        }
        //
        //        public Set keySet() {
        //                return requestAttributes.keySet();
        //        }
        //
        //        public Collection values() {
        //                return requestAttributes.values();
        //        }
        //
        //        public Set entrySet() {
        //                return requestAttributes.entrySet();
        //        }

}








