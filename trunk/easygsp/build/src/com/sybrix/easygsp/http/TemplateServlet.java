package com.sybrix.easygsp.http;

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


import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.servlet.AbstractHttpServlet;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Binding;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sybrix.easygsp.http.CustomServletBinding;
import com.sybrix.easygsp.exception.TemplateNotFoundException;
import org.codehaus.groovy.control.CompilationFailedException;

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
 *   generate.by = true(default) | false
 * </pre>
 *
 * @author Christian Stein
 * @author Guillaume Laforge
 * @version 2.0
 */
public class TemplateServlet extends AbstractHttpServlet {
        private static Pattern INCLUDE_REGEX = Pattern.compile("<%@\\s*include\\s*file\\s*=\\s*\"[\\w|\\\\|\\//||.]+\"\\s*%>");

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
                List children;
                                                                               
                public TemplateCacheEntry(File file, Template template) {
                        this(file, template, false);// don't get time millis for sake of speed
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
                }

                public void addChild(String key) {
                        if (children == null)
                                children = new ArrayList();

                        children.add(key);
                }

                public List<String> getChildren() {
                        return children;
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

        /**
         * Underlying template engine used to evaluate template source files.
         */
        private TemplateEngine includeTemplateEngine;

        /**
         * Flag that controls the appending of the "Generated by ..." comment.
         */
        private boolean generateBy;

        /**
         * Create new TemplateSerlvet.
         */
        public TemplateServlet(GSE3 groovyScriptEngine) {
                this.cache = new WeakHashMap();
                //this.includeTemplateEngine = new IncludeTemplateEngine();// assigned later by init()
                this.includeTemplateEngine = new IncludeTemplateEngine(groovyScriptEngine);// assigned later by init()
                this.generateBy = true;// may be changed by init()
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
        protected Template getTemplate(File file, String requestedUrl, Binding binding) throws ServletException {

                String key = file.getAbsolutePath();
                Template template = null;

                RequestThreadInfo.get().setRealScriptName(key.substring(key.lastIndexOf(File.separator) + 1));

                /*
                * Test cache for a valid template bound to the key.
                */
                if (verbose) {
                        log("Looking for cached template by key \"" + key + "\"");
                }
                TemplateCacheEntry entry = (TemplateCacheEntry) cache.get(key);
                List<String> children = null;

                if (entry == null) {
                        children = new ArrayList();
                } else if (entry.getChildren() == null) {
                        children = new ArrayList();
                } else {
                        children = entry.getChildren();
                }

                TemplateTL.get().setCache(children);


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
                        if (verbose) {
                                log("Cache miss.");
                        }
                }

                if (template != null && entry != null && !children.isEmpty()) {
                        for (String childKey : children) {
                                TemplateCacheEntry childEntry = (TemplateCacheEntry) cache.get(childKey);

                                if (childEntry != null) {
                                        if (!childEntry.validate(new File(childKey))) {
                                                template = null;
                                                if (verbose) {
                                                        log("Child template changed, Cached template needs recompiliation!");
                                                }

                                                break;
                                        }
                                } else {
                                        if (verbose) {
                                                log("Cache miss.");
                                        }
                                }
                        }
                }
                //
                // Template not cached or the source file changed - compile new template!
                //
                if (template == null) {
                        TemplateTL.get().setSourceNewer(true);

                        if (verbose) {
                                log("Creating new template from file " + file + "...");
                        }

                        Reader reader = null;
                        try {
                                RequestThreadInfo.get().setCurrentFile(file.getAbsolutePath());
                                reader = new FileReader(file);
                                children.clear();
                                template = ((IncludeTemplateEngine) includeTemplateEngine).createTemplate(reader, requestedUrl, file.getPath(), binding);
                        } catch (CompilationFailedException e) {
                                throw e;
                        } catch (GroovyRuntimeException e) {
                                throw e;
                        } catch (Throwable e) {
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
                        entry = new TemplateCacheEntry(file, template, verbose);
                        cache.put(key, entry);
                        if (verbose) {
                                log("Created and added template to cache. [key=" + key + "]");
                        }

                        if (children != null) {
                                for (String child : children) {
                                        cache.put(child, new TemplateCacheEntry(new File(child), template, verbose));
                                        entry.addChild(child);
                                }
                        }
                }

                RequestThreadInfo.get().setUniqueScriptName(((IncludeTemplateEngine.SimpleTemplate)template).getTemplateName());

                //
                // Last sanity check.
                //
                if (template == null) {
                        throw new ServletException("Template is null? Should not happen here!");
                }

                return template;

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
        //        public void init(ServletConfig config) throws ServletException {
        //                super.init(config);
        //                this.engine = initTemplateEngine(config);
        //                if (engine == null) {
        //                        throw new ServletException("Template engine not instantiated.");
        //                }
        //                String value = config.getInitParameter("generated.by");
        //                if (value != null) {
        //                        this.generateBy = Boolean.valueOf(value).booleanValue();
        //                }
        //                log("Servlet " + getClass().getName() + " initialized on " + engine.getClass());
        //        }

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
        //        protected TemplateEngine initTemplateEngine(ServletConfig config) {
        //                String name = config.getInitParameter("template.engine");
        //                if (name == null) {
        //                        return new SimpleTemplateEngine();
        //                }
        //                try {
        //                        return (TemplateEngine) Class.forName(name).newInstance();
        //                } catch (InstantiationException e) {
        //                        log("Could not instantiate template engine: " + name, e);
        //                } catch (IllegalAccessException e) {
        //                        log("Could not access template engine class: " + name, e);
        //                } catch (ClassNotFoundException e) {
        //                        log("Could not find template engine class: " + name, e);
        //                }
        //                return null;
        //        }

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
        public void service(String templatePath, HttpServletRequest request, HttpServletResponse response, CustomServletBinding binding) throws ServletException, IOException {
                RequestThreadInfo.get().setScriptProcessed(true);
                
                if (verbose) {
                        log("Creating/getting cached template...");
                }

                //
                // Get the template source file handle.
                //
                //File file = super.getScriptUriAsFile(request);
                //Application application = (Application) binding.getVariable("application");

                File file = null;
                if (File.separator.equals("\"")) {
                        file = new File(ThreadAppIdentifier.get().getAppPath()+ File.separator + templatePath.replaceAll("/", "\\\\"));
                } else {
                        file = new File(ThreadAppIdentifier.get().getAppPath() + File.separator + templatePath);
                }

                RequestThreadInfo.get().setCurrentFile(file.getAbsolutePath());
                
                //Reader rd = ThreadFileReader.get();
                String name = file.getName();
                //if (rd == null) {
                        if (!file.exists()) {
                                //response.sendError(HttpServletResponse.SC_NOT_FOUND);
                                throw new TemplateNotFoundException(file.toString());
                                //return;// throw new IOException(file.getAbsolutePath());
                        }
                        if (!file.canRead()) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Can not read \"" + name + "\"!");
                                return;// throw new IOException(file.getAbsolutePath());
                        }
                //}
                //
                // Get the requested template.
                //
                //long getMillis = System.currentTimeMillis();
                Template template = getTemplate(file, templatePath, binding);
                //getMillis = System.currentTimeMillis() - getMillis;


                //
                // Create new binding for the current request.
                //
                //ServletBinding binding = new ServletBinding(request, response, servletContext);
                //setVariables(binding);

                //
                // Prepare the response buffer content type _before_ getting the writer.
                // and set status code to ok
                //
                response.setContentType(CONTENT_TYPE_TEXT_HTML + "; charset=" + response.getCharacterEncoding());
                response.setStatus(HttpServletResponse.SC_OK);

                //
                // Get the output stream writer from the binding.
                //
                Writer out = (Writer) binding.getVariable("out");
                if (out == null) {
                        out = response.getWriter();
                }

                //
                // Evaluate the template.
                //
                if (verbose) {
                        log("Making template \"" + name + "\"...");
                }
                //String made = template.make(binding.getVariables()).toString();
                // log(" = " + made);
//                long makeMillis = System.currentTimeMillis();
                template.make(binding.getVariables()).writeTo(out);
//                makeMillis = System.currentTimeMillis() - makeMillis;
//
//                if (generateBy) {
//                        StringBuffer sb = new StringBuffer(100);
//                        sb.append("\n<!-- Generated by Groovy TemplateServlet [create/get=");
//                        sb.append(Long.toString(getMillis));
//                        sb.append(" ms, make=");
//                        sb.append(Long.toString(makeMillis));
//                        sb.append(" ms] -->\n");
//                        out.write(sb.toString());
//                }
//
//                //
//                // flush the response buffer.
//                //
//                //response.flushBuffer();
//
//                if (verbose) {
//                        log("Template \"" + name + "\" request responded. [create/get=" + getMillis + " ms, make=" + makeMillis + " ms]");
//                }

        }

        /**
         * Override this method to set your variables to the Groovy binding.
         * <p>
         * All variables bound the binding are passed to the template source text,
         * e.g. the HTML file, when the template is merged.
         * </p>
         * <p>
         * The binding provided by TemplateServlet does already include some default
         * variables. As of this writing, they are (copied from
         * {@link groovy.servlet.ServletBinding}):
         * <ul>
         * <li><tt>"request"</tt> : HttpServletRequest </li>
         * <li><tt>"response"</tt> : HttpServletResponse </li>
         * <li><tt>"context"</tt> : ServletContext </li>
         * <li><tt>"application"</tt> : ServletContext </li>
         * <li><tt>"session"</tt> : request.getSession(<b>false</b>) </li>
         * </ul>
         * </p>
         * <p>
         * And via implicite hard-coded keywords:
         * <ul>
         * <li><tt>"out"</tt> : response.getWriter() </li>
         * <li><tt>"sout"</tt> : response.getOutputStream() </li>
         * <li><tt>"html"</tt> : new MarkupBuilder(response.getWriter()) </li>
         * </ul>
         * </p>
         * <p/>
         * <p>Example binding all servlet context variables:
         * <pre><code>
         * class Mytlet extends TemplateServlet {
         * <p/>
         *   protected void setVariables(ServletBinding binding) {
         *     // Bind a simple variable
         *     binding.setVariable("answer", new Long(42));
         * <p/>
         *     // Bind all servlet context attributes...
         *     ServletContext context = (ServletContext) binding.getVariable("context");
         *     Enumeration enumeration = context.getAttributeNames();
         *     while (enumeration.hasMoreElements()) {
         *       String name = (String) enumeration.nextElement();
         *       binding.setVariable(name, context.getAttribute(name));
         *     }
         *   }
         * <p/>
         * }
         * <code></pre>
         * </p>
         *
         * @param binding to be modified
         */
        protected void setVariables(CustomServletBinding binding) {
                // empty
        }

        //        private StringBuffer readTemplateFile(String filePath){
        //
        //        }

}
