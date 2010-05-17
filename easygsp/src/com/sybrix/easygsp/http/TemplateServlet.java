/*
 * Copyright 2003-2007 the original author or authors.
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

import groovy.servlet.AbstractHttpServlet;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.lang.GroovyRuntimeException;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sybrix.easygsp.util.StringUtil;

/**
 * A generic servlet for serving (mostly HTML) templates.
 * <p/>
 * <p/>
 * It delegates work to a <code>groovy.text.TemplateEngine</code> implementation
 * processing HTTP requests.
 * <p/>
 * <h4>Usage</h4>
 * <p/>
 * <code>helloworld.html</code> is a headless HTML-like template
 * <pre><code>
 *  &lt;html&gt;
 *    &lt;body&gt;
 *      &lt;% 3.times { %&gt;
 *        Hello World!
 *      &lt;% } %&gt;
 *      &lt;br&gt;
 *    &lt;/body&gt;
 *  &lt;/html&gt;
 * </code></pre>
 * <p/>
 * Minimal <code>web.xml</code> example serving HTML-like templates
 * <pre><code>
 * &lt;web-app&gt;
 *   &lt;servlet&gt;
 *     &lt;servlet-name&gt;template&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;groovy.servlet.TemplateServlet&lt;/servlet-class&gt;
 *   &lt;/servlet&gt;
 *   &lt;servlet-mapping&gt;
 *     &lt;servlet-name&gt;template&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;*.html&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;
 * </code></pre>
 * <p/>
 * <h4>Template engine configuration</h4>
 * <p/>
 * <p/>
 * By default, the TemplateServer uses the {@link groovy.text.SimpleTemplateEngine}
 * which interprets JSP-like templates. The init parameter <code>template.engine</code>
 * defines the fully qualified class name of the template to use:
 * <pre>
 *   template.engine = [empty] - equals groovy.text.SimpleTemplateEngine
 *   template.engine = groovy.text.SimpleTemplateEngine
 *   template.engine = groovy.text.GStringTemplateEngine
 *   template.engine = groovy.text.XmlTemplateEngine
 * </pre>
 * <p/>
 * <h4>Logging and extra-output options</h4>
 * <p/>
 * <p/>
 * This implementation provides a verbosity flag switching log statements.
 * The servlet init parameter name is:
 * <pre>
 *   generated.by = true(default) | false
 * </pre>
 *
 * @author Christian Stein
 * @author Guillaume Laforge
 * @version 2.0
 * @see TemplateServlet#setVariables(groovy.servlet.ServletBinding)
 */
public class TemplateServlet extends AbstractHttpServlet {

        /**
         * Simple cache entry that validates against last modified and length
         * attributes of the specified file.
         *
         * @author Christian Stein
         */
        public static class TemplateCacheEntry {

                Date date;
                long hit;
                long lastModified;
                long length;
                Template template;
                Set children;


                public TemplateCacheEntry(File file, Template template) {
                        this(file, template, false); // don't get time millis for sake of speed
                }

                public TemplateCacheEntry(File file, Template template, boolean timestamp) {
                        if (file == null) {
                                throw new NullPointerException("file");
                        }
                        if (template == null) {
                                throw new NullPointerException("template");
                        }
                        if (timestamp) {
                                this.date = new Date(System.currentTimeMillis());
                        } else {
                                this.date = null;
                        }
                        this.hit = 0;
                        this.lastModified = file.lastModified();
                        this.length = file.length();
                        this.template = template;
                        this.children = new HashSet();
                }

                public Set getChildren() {
                        return children;
                }
                public void setChildren(Set children) {
                        this.children = children;
                }

                /**
                 * Checks the passed file attributes against those cached ones.
                 *
                 * @param file Other file handle to compare to the cached values.
                 * @return <code>true</code> if all measured values match, else <code>false</code>
                 */
                public boolean validate(File file) {
                        if (file == null) {
                                throw new NullPointerException("file");
                        }
                        if (file.lastModified() != this.lastModified) {
                                return false;
                        }
                        if (file.length() != this.length) {
                                return false;
                        }
                        hit++;
                        return true;
                }

                public String toString() {
                        if (date == null) {
                                return "Hit #" + hit;
                        }
                        return "Hit #" + hit + " since " + date;
                }

        }

        /**
         * Simple file name to template cache map.
         */
        private final Map cache;
        private final Map dependencyCache;

        /**
         * Underlying template engine used to evaluate template source files.
         */
        private TemplateEngine engine;

        /**
         * Flag that controls the appending of the "Generated by ..." comment.
         */
        private boolean generateBy;

        /**
         * Create new TemplateSerlvet.
         */
        public TemplateServlet(GSE4 groovyScriptEngine) {
                this.cache = Collections.synchronizedMap(new HashMap());
                this.dependencyCache = Collections.synchronizedMap(new HashMap());

                this.engine = new IncludeTemplateEngine(groovyScriptEngine.getGroovyClassLoader()); // assigned later by init()
                this.generateBy = true; // may be changed by init()

        }

        /**
         * Gets the template created by the underlying engine parsing the request.
         * <p/>
         * <p>
         * This method looks up a simple (weak) hash map for an existing template
         * object that matches the source file. If the source file didn't change in
         * length and its last modified stamp hasn't changed compared to a precompiled
         * template object, this template is used. Otherwise, there is no or an
         * invalid template object cache entry, a new one is created by the underlying
         * template engine. This new instance is put to the cache for consecutive
         * calls.
         * </p>
         *
         * @param file The HttpServletRequest.
         * @return The template that will produce the response text.
         * @throws ServletException If the request specified an invalid template source file
         */
        protected Template getTemplate(File file) throws ServletException {

                String key = file.getAbsolutePath();
                Template template = null;

                /*
                * Test cache for a valid template bound to the key.
                */
//                if (verbose) {
//                        System.out.println("Looking for cached template by key \"" + key + "\"");
//                }
                TemplateCacheEntry entry = (TemplateCacheEntry) cache.get(key);
                if (entry != null) {
                        if (entry.validate(file)) {
                                if (verbose) {
                                        log("Cache hit! " + entry);
                                }
                                template = entry.template;
                        } else {
                                if (verbose) {
                                        log("Cached template needs recompiliation!");
                                }
                        }

                } else {
                       // if (verbose) {
                               // System.out.println("Cache miss.");
                        //}
                }

                //
                // Template not cached or the source file changed - compile new template!
                //
                if (template == null || RequestThreadInfo.get().isCodeBehindNewer()) {

                        if (verbose) {
                                log("Creating new template from file " + file + "...");
                        }

                        String fileEncoding = System.getProperty("groovy.source.encoding");

                        Reader reader = null;
                        try {
                                reader = fileEncoding == null ? new FileReader(file) : new InputStreamReader(new FileInputStream(file), fileEncoding);
                                template = engine.createTemplate(reader);
                        } catch (GroovyRuntimeException e) {
                                throw e;
                        } catch (Exception e) {
                                throw new ServletException("Creation of template failed: " + e, e);
                        } finally {
                                if (reader != null) {
                                        try {
                                                reader.close();
                                        } catch (IOException ignore) {
                                                // e.printStackTrace();
                                        }
                                }
                        }

                        TemplateCacheEntry templateCacheEntry = new TemplateCacheEntry(file, template, verbose);
                        templateCacheEntry.children.addAll(RequestThreadInfo.get().getTemplateInfo().getChildren());
                        //RequestThreadInfo.get().getTemplateInfo().setCachEntry(templateCacheEntry);
                        cache.put(key, templateCacheEntry);

                        for (Object o : templateCacheEntry.children) {
                                dependencyCache.put(o, key);
                        }

                        if (verbose) {
                                log("Created and added template to cache. [key=" + key + "]");
                        }
                }

                //
                // Last sanity check.
                //
                if (template == null) {
                        throw new ServletException("Template is null? Should not happen here!");
                }

                return template;

        }

        public void removeFromCache(String template) {
                template = StringUtil.capDriveLetter(template);
                Object key = dependencyCache.remove(template);
                if (key != null) {
                        TemplateCacheEntry templateCacheEntry = (TemplateCacheEntry) cache.remove(key);
                        if (templateCacheEntry != null) {
                                for (Object o : templateCacheEntry.children) {
                                        dependencyCache.remove(o);
                                }
                        }
                } else {
                        cache.remove(template);
                }
        }



        /**
         * Initializes the servlet from hints the container passes.
         * <p/>
         * Delegates to sub-init methods and parses the following parameters:
         * <ul>
         * <li> <tt>"generatedBy"</tt> : boolean, appends "Generated by ..." to the
         * HTML response text generated by this servlet.
         * </li>
         * </ul>
         *
         * @param config Passed by the servlet container.
         * @throws ServletException if this method encountered difficulties
         * @see TemplateServlet#initTemplateEngine(ServletConfig)
         */
        public void init(ServletConfig config) throws ServletException {
                super.init(config);
                this.engine = initTemplateEngine(config);
                if (engine == null) {
                        throw new ServletException("Template engine not instantiated.");
                }
                String value = config.getInitParameter("generated.by");
                if (value != null) {
                        this.generateBy = Boolean.valueOf(value).booleanValue();
                }
                log("Servlet " + getClass().getName() + " initialized on " + engine.getClass());
        }

        /**
         * Creates the template engine.
         * <p/>
         * Called by {@link TemplateServlet#init(ServletConfig)} and returns just
         * <code>new groovy.text.SimpleTemplateEngine()</code> if the init parameter
         * <code>template.engine</code> is not set by the container configuration.
         *
         * @param config Current serlvet configuration passed by the container.
         * @return The underlying template engine or <code>null</code> on error.
         */
        protected TemplateEngine initTemplateEngine(ServletConfig config) {
                String name = config.getInitParameter("template.engine");
                if (name == null) {
                        return new SimpleTemplateEngine();
                }
                try {
                        return (TemplateEngine) Class.forName(name).newInstance();
                } catch (InstantiationException e) {
                        log("Could not instantiate template engine: " + name, e);
                } catch (IllegalAccessException e) {
                        log("Could not access template engine class: " + name, e);
                } catch (ClassNotFoundException e) {
                        log("Could not find template engine class: " + name, e);
                }
                return null;
        }

        /**
         * Services the request with a response.
         * <p>
         * First the request is parsed for the source file uri. If the specified file
         * could not be found or can not be read an error message is sent as response.
         * <p/>
         * </p>
         *
         * @param request  The http request.
         * @param response The http response.
         * @throws IOException      if an input or output error occurs while the servlet is
         *                          handling the HTTP request
         * @throws ServletException if the HTTP request cannot be handled
         */
        public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FileNotFoundException {
                if (!RequestThreadInfo.get().errorOccurred())
                        RequestThreadInfo.get().setTemplateRequest(true);

                if (verbose) {
                        log("Creating/getting cached template...");
                }

                //
                // Get the template source file handle.
                //
                //File file = super.getScriptUriAsFile(request);
                File file = new File(RequestThreadInfo.get().getParsedRequest().getRequestFilePath());
                if (!RequestThreadInfo.get().errorOccurred())
                        RequestThreadInfo.get().setCurrentFile(file.getAbsolutePath());

                String name = file.getName();
                if (!file.exists()) {
                        throw new FileNotFoundException("file " + file.getAbsolutePath() + " not found");
                }
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            return; // throw new IOException(file.getAbsolutePath());
//        }
//        if (!file.canRead()) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Can not read \"" + name + "\"!");
//            return; // throw new IOException(file.getAbsolutePath());
//        }

                //
                // Get the requested template.
                //
                long getMillis = System.currentTimeMillis();

                Template template = getTemplate(file);
                String templateName = ((IncludeTemplateEngine.SimpleTemplate) template).getScript().getClass().getName();
                if (!RequestThreadInfo.get().errorOccurred()) {
                        RequestThreadInfo.get().setUniqueTemplateScriptName(templateName);
                }
                getMillis = System.currentTimeMillis() - getMillis;
                   
                //
                // Create new binding for the current request.
                //
                //ServletBinding binding = new ServletBinding(request, response, servletContext);
                CustomServletBinding binding = RequestThreadInfo.get().getBinding();
                //setVariables(RequestThreadInfo.get().getBinding());

                //
                // Prepare the response buffer content type _before_ getting the writer.
                // and set status code to ok
                //
                response.setContentType(CONTENT_TYPE_TEXT_HTML + "; charset=" + encoding);
                //response.setStatus(HttpServletResponse.SC_OK);

                //
                // Get the output stream writer from the binding.
                //
                Writer out = response.getWriter();
                //if (out == null) {
                //    out = response.getWriter();
                //}

                //
                // Evaluate the template.
                //
//        if (verbose) {
//            log("Making template \"" + name + "\"...");
//        }
                // String made = template.make(binding.getVariables()).toString();
                // log(" = " + made);
                long makeMillis = System.currentTimeMillis();
                template.make(binding.getVariables()).writeTo(out);
                makeMillis = System.currentTimeMillis() - makeMillis;

//        if (generateBy) {
//            StringBuffer sb = new StringBuffer(100);
//            sb.append("\n<!-- Generated by Groovy TemplateServlet [create/get=");
//            sb.append(Long.toString(getMillis));
//            sb.append(" ms, make=");
//            sb.append(Long.toString(makeMillis));
//            sb.append(" ms] -->\n");
//            out.write(sb.toString());
//        }

                //
                // flush the response buffer.
                //
                //response.flushBuffer();

//        if (verbose) {
//            log("Template \"" + name + "\" request responded. [create/get=" + getMillis + " ms, make=" + makeMillis + " ms]");
//        }

        }
}
