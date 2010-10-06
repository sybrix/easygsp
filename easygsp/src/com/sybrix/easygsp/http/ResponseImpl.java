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

import com.sybrix.easygsp.exception.NotImplementedException;
import com.sybrix.easygsp.server.EasyGServer;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;

/**
 * HttpSession <br/>
 * Description :
 */
public class ResponseImpl implements HttpServletResponse {
        private static final Logger log = Logger.getLogger(ResponseImpl.class.getName());
        private Map<String, Object> headers;
        private int statusCode = 0;

        public static final SimpleDateFormat cookieDateFormatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z");
        public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");

        private ServletOutputStreamImpl outputStream;
        private OutputStream bufferedOutputStream;

        //private ByteArrayOutputStream byteArrayBuffer;
        private ByteArrayOutputStream byteArrayBuffer;
        private PrintWriter printWriterBuffer;

        private int bufferSize;
        List<Cookie> cookies;
        private String status;
        private String characterEncoding = "UTF-8";
        //private Request request;
        private boolean headersFlushed;
        private BufferedWriter printWriter;
        private Locale locale;
        private boolean committed;
        private HttpServletRequest request;

        static {
                cookieDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        public ResponseImpl(OutputStream outputStream) {
                headers = new HashMap();

                // this.request = request;
                this.outputStream = new ServletOutputStreamImpl(outputStream);
                characterEncoding = EasyGServer.propertiesFile.getString("default.charset");
                //headers.put(ResponseHeaders.CONTENT_TYPE, "text/html; charset=" + characterEncoding);
                bufferSize = EasyGServer.propertiesFile.getInt("output.buffer.size");
                // bufferedOutputStream = new BufferedOutputStream(outputStream);
        }

        protected void setHttpServletRequest(HttpServletRequest request) {
                this.request = request;
        }

        public void addCookie(Cookie cookie) {
                if (cookies == null)
                        this.cookies = new ArrayList();

                cookies.add(cookie);
        }

        public boolean containsHeader(String header) {
                return headers.containsKey(header);
        }

        public String encodeURL(String s) {
                throw new NotImplementedException("Response.encodeURL() is not implemented");
        }

        public String encodeRedirectURL(String s) {
                throw new NotImplementedException("Response.encodeRedirectURL() is not implemented");
        }

        public String encodeUrl(String s) {
                throw new NotImplementedException("Response.encodeUrl() is not implemented");
        }

        public String encodeRedirectUrl(String s) {
                throw new NotImplementedException("Response.encodeRedirectUrl() is not implemented");

        }

        public void sendError(int statusCode, String status) throws IOException {
                this.statusCode = statusCode;
                this.status = status;
        }

        public void sendError(int statusCode) throws IOException {
                this.statusCode = statusCode;
        }

        public void sendRedirect(String s) throws IOException {
                //HTTP/1.1 302 Found
                //Server: Resin/3.1.9
                //Location: http://localhost:8085/scgi/hey.jsp
                //Content-Type: text/html; charset=UTF-8
                //Content-Length: 72
                //Date: Wed, 01 Jul 2009 12:53:21 GMT
                //
                boolean secure = request.isSecure();
                //@todo - fix for apache
                String script = request.getHeader(RequestHeaders.SCRIPT_NAME);
                String scriptPath = script.substring(0, script.lastIndexOf("/"));
                String server = request.getHeader(RequestHeaders.SERVER_NAME);
                String serverPort = ":" + request.getHeader(RequestHeaders.SERVER_PORT);
                if (serverPort.equals(":80")) {
                        serverPort = "";
                }
                String url = null;

                if (s.startsWith("https://") || s.startsWith("http://")) {
                        url = s;
                } else {
                        if (EasyGServer.propertiesFile.getBoolean("virtual.hosting", false)) {
                                if (s.startsWith("/"))
                                        url = (secure ? "https://" : "http://") + server + serverPort + s;
                                else {
                                        url = (secure ? "https://" : "http://") + server + serverPort + scriptPath + "/" + s;
                                }

                                log.finest("redirect: " + url);
                        } else {
                                if (s.startsWith("/"))
                                        url = (secure ? "https://" : "http://") + server + serverPort + scriptPath + s;
                                else
                                        url = (secure ? "https://" : "http://") + server + serverPort + scriptPath + "/" + s;
                        }

                        log.log(Level.FINEST, "url:" + url + ", scriptPath: " + scriptPath);
                }

                getByteArrayBuffer().write(("The URL has moved <a href=\"" + url + "\">here</a>").getBytes());

                setStatus(302, "Found");

                setHeader("Location", url);

                flushBuffer();
                //throw new NotImplementedException("Response.sendRedirect() is not implemented");
        }

        protected void clearBuffer() {
                if (byteArrayBuffer != null)
                        byteArrayBuffer.reset();
        }

        protected void flushWriter() {
                try {
                        getWriter().flush();
                } catch (IOException e) {

                }
        }

        public void setDateHeader(String header, long value) {
                Date date = new Date(value);
                setHeader(header, dateFormatter.format(date));
        }

        public void out(String s) {
                getByteArrayBuffer().write(s.getBytes(), 0, s.getBytes().length);
        }

        public void addDateHeader(String header, long value) {
                Date date = new Date(value);
                addHeader(header, dateFormatter.format(date));
        }

        public void setHeader(String header, String value) {
                headers.put(header, value);
        }

        public void addHeader(String header, String value) {
                Object headerValue = headers.get(header);
                if (headerValue == null) {
                        setHeader(header, value);
                } else if (headerValue instanceof String) {
                        List values = new ArrayList();
                        values.add(headerValue);
                        values.add(value);
                        headers.put(header, values);
                } else if (headerValue instanceof List) {
                        ((List) headerValue).add(value);
                }
        }

        public void setIntHeader(String header, int value) {
                setHeader(header, String.valueOf(value));
        }

        public void setLongHeader(String header, long value) {
                setHeader(header, String.valueOf(value));
        }

        public void addIntHeader(String header, int value) {
                addHeader(header, String.valueOf(value));
        }

        public void setStatus(int statusCode) {
                this.statusCode = statusCode;
        }

        protected int getStatusCode() {
                return statusCode;
        }

        public void setStatus(int i, String s) {
                statusCode = i;
                status = s;
        }

        public String getCharacterEncoding() {
                return characterEncoding;
        }

        public String getContentType() {
                return (String) headers.get(ResponseHeaders.CONTENT_TYPE);
        }

        public ServletOutputStream getOutputStream() throws IOException {
                return outputStream;
        }

        public OutputStream getBufferedOutputStream() throws IOException {
                return bufferedOutputStream;
        }

        public PrintWriter getWriter() throws IOException {
                if (printWriterBuffer == null) {
                        printWriterBuffer = new PrintWriter(getByteArrayBuffer(), true);
                }

                return printWriterBuffer;
        }

        public void setCharacterEncoding(String s) {
                characterEncoding = s;
        }

        public void setContentLength(int contentLength) {
                setHeader(ResponseHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        }

        public void setContentType(String contentType) {
                setHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        }

        public void setBufferSize(int bufferSize) {
                this.bufferSize = bufferSize;
                if (byteArrayBuffer == null) {
                        byteArrayBuffer = new ByteArrayOutputStream(bufferSize);
                } else {
                        throw new java.lang.IllegalStateException("Must be set before any content is written");
                }
        }

        private ByteArrayOutputStream getByteArrayBuffer() {
                if (byteArrayBuffer == null) {
                        byteArrayBuffer = new ByteArrayOutputStream(bufferSize);
                }
                return byteArrayBuffer;
        }

        protected Integer getBufferContentSize() {
                if (byteArrayBuffer == null)
                        return null;

                return byteArrayBuffer.size();
        }

        public int getBufferSize() {
                return bufferSize;
        }

        public void flushBuffer() throws IOException {
                sendResponse();
                outputStream.flush();
        }

        public void resetBuffer() {
                throw new NotImplementedException("Response.resetBuffer() is not implemented");
        }

        public boolean isCommitted() {
                return committed;
        }

        public void reset() {
                if (isCommitted())
                        throw new IllegalStateException("Response already committed");

                byteArrayBuffer.reset();
        }

        public void setLocale(Locale locale) {
                this.locale = locale;
        }

        public Locale getLocale() {
                return locale;
        }

        class ServletOutputStreamImpl extends ServletOutputStream {
                private OutputStream outputStream;

                public ServletOutputStreamImpl(OutputStream o) {
                        outputStream = o;
                }

                public void write(int b) throws IOException {
                        outputStream.write(b);
                }

                public void write(byte b[], int off, int len) throws IOException {
                        outputStream.write(b, off, len);
                }

                public OutputStream getOutputStream() {
                        return outputStream;
                }

        }

        protected void sendResponse() {
                try {
                        if (printWriter == null) {
                                printWriter = new BufferedWriter(new OutputStreamWriter(outputStream, characterEncoding));
                        }

                        if (!headersFlushed) {
                                sendHeaders(printWriter);
                                headersFlushed = true;
                        }
                        if (byteArrayBuffer != null && byteArrayBuffer.size() > 0) {
                                printWriter.write(getByteArrayBuffer().toString());
                                byteArrayBuffer.reset();
                                printWriter.flush();
                        }
                } catch (UnsupportedEncodingException e) {
                        log.log(Level.SEVERE, "Unsupported charset: " + characterEncoding, e);
                } catch (IOException e) {
                        log.log(Level.SEVERE, "sendResponse() failed: ", e);
                } catch (Exception e) {
                        log.log(Level.SEVERE, "sendResponse() failed: ", e);
                }
        }

        private void sendHeaders(BufferedWriter printWriter) throws IOException {
                if (getByteArrayBuffer().size() > 0 && !headers.containsKey(ResponseHeaders.CONTENT_LENGTH)) {
                        setIntHeader(ResponseHeaders.CONTENT_LENGTH, getByteArrayBuffer().size());
                }

                try {
                        committed = true;
                        printWriter.write(createResponseHeaders());
                        printWriter.flush();
                } catch (IOException e) {
                        throw new IOException("sendHeaders() failed", e);
                } catch (Throwable e) {
                        e.printStackTrace();
                }
        }

        private String createResponseHeaders() {

                if (cookies != null && cookies.size() > 0) {
                        for (Cookie cookie : cookies) {
                                addHeader(ResponseHeaders.SET_COOKIE, CookieUtil.format(cookie));
                        }
                }

                StringBuffer buffer = new StringBuffer(1000);
                if (statusCode == 0)
                        statusCode = 200;

                if (statusCode == 200)
                        status = "OK";

                headers.put("Status", String.valueOf(statusCode));

                //buffer.append(ResponseHeaders.STATUS).append(":HTTP/1.1 ").append(statusCode).append(" ").append(status).append(ResponseHeaders.END_OF_LINE);
                //buffer.append("HTTP/1.1 ").append(statusCode).append(" ").append(status).append(ResponseHeaders.END_OF_LINE);
                for (String header : headers.keySet()) {
                        Object obj = headers.get(header);
                        if (obj instanceof String) {
                                buffer.append(header).append(": ").append(obj).append(ResponseHeaders.END_OF_LINE);
                        } else if (obj instanceof List) {
                                for (Object value : (List) obj) {
                                        buffer.append(header).append(": ").append(value).append(ResponseHeaders.END_OF_LINE);
                                }
                        }
                }

                buffer.append(ResponseHeaders.END_OF_LINE);

                return buffer.toString();
        }

}
