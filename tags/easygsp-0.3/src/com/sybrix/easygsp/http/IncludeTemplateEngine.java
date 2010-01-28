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

import groovy.text.TemplateEngine;
import groovy.text.Template;
import groovy.lang.*;

import java.io.*;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import com.sybrix.easygsp.http.StaticControllerMethods;

/**
 * http://www.antoniogoncalves.org/xwiki/bin/view/Blog/TemplateWithTemplatesInGroovy
 * <p/>
 * This simple template engine uses JSP <% %> script, <%= %> and <@ @> expression syntax.  It also lets you use normal groovy expressions in
 * the template text much like the new JSP EL functionality.  The variable 'out' is bound to the writer that the template is being written to.
 *
 * @author sam
 * @author Christian Stein
 * @author Paul King
 */
public class IncludeTemplateEngine extends TemplateEngine {
        private static final Logger log = Logger.getLogger(IncludeTemplateEngine.class.getName());
        private boolean verbose;
        private static int counter = 1;
        private GroovyShell groovyShell;

        //private GSE4 groovyScriptEngine;

//        public IncludeTemplateEngine() {
//                this(GroovyShell.class.getClassLoader());
//        }

        public IncludeTemplateEngine(GSE4 groovyScriptEngine) {
                this(groovyScriptEngine.getGroovyClassLoader());
                //this.groovyScriptEngine = groovyScriptEngine;


        }

        public IncludeTemplateEngine(boolean verbose) {
                this(GroovyShell.class.getClassLoader());
                setVerbose(verbose);
        }

        public IncludeTemplateEngine(ClassLoader parentLoader) {
                this(new GroovyShell(parentLoader));

        }

        public IncludeTemplateEngine(GroovyShell groovyShell) {
              this.groovyShell = groovyShell;
        }

//        public Template createTemplate(Reader reader) throws CompilationFailedException, IOException {
//                //return createTemplate(reader, null);
//                return null;
//        }

        public Template createTemplate(Reader reader) throws CompilationFailedException, IOException {
                SimpleTemplate template = new SimpleTemplate();
                String script = template.parse(reader, true);
                try {

                        //template.script = groovyShell.parse(script, "SimpleTemplateScript" + counter++ + ".groovy");

                        String uniqueScriptName = "SimpleTemplateScript" + counter++ + ".groovy";
                        RequestThreadInfo.get().setUniqueScriptName(uniqueScriptName.substring(0, uniqueScriptName.length() - 7));
                        //template.setTemplateName(uniqueScriptName.substring(0, uniqueScriptName.length() - 7));


                        template.script = groovyShell.parse(script, uniqueScriptName);
                        //template.script = groovyScriptEngine.createScript(script,requestedUrl, uniqueScriptName, binding);
                        StaticControllerMethods.addMethods(template.script.getClass());
                } catch (Exception e) {
                        throw new GroovyRuntimeException("Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): " + e.getMessage());
                }

                return template;
        }

        public Template createTemplate(Reader reader, String requestedUrl, String scriptFileName, Binding binding) throws CompilationFailedException, IOException {
                SimpleTemplate template = new SimpleTemplate();
                String script = template.parse(reader, true);


//                if (verbose) {
//                        System.out.println("\n-- script source --");
//                        System.out.print(script);
//                        System.out.println("\n-- script end --\n");
//                }

                try {

                        //template.script = groovyShell.parse(script, "SimpleTemplateScript" + counter++ + ".groovy");

                        String uniqueScriptName = "SimpleTemplateScript" + counter++ + ".groovy";
                        RequestThreadInfo.get().setUniqueScriptName(uniqueScriptName.substring(0, uniqueScriptName.length() - 7));
                        //template.setTemplateName(uniqueScriptName.substring(0, uniqueScriptName.length() - 7));


                        template.script = groovyShell.parse(script, uniqueScriptName);
                        //template.script = groovyScriptEngine.createScript(script,requestedUrl, uniqueScriptName, binding);
                        StaticControllerMethods.addMethods(template.script.getClass());
                } catch (Exception e) {
                        throw new GroovyRuntimeException("Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): " + e.getMessage());
                }
                return template;
        }

        public void setVerbose(boolean verbose) {
                this.verbose = verbose;
        }

        public boolean isVerbose() {
                return verbose;
        }

        protected static class SimpleTemplate implements Template {
                protected Script script;

                public Writable make() {
                        return make(null);
                }

                private String templateName;

                public String getTemplateName() {
                        return templateName;
                }

                public void setTemplateName(String templateName) {
                        this.templateName = templateName;
                }

                public Writable make(final Map map) {
                        return new Writable() {
                                /**
                                 * Write the template document with the set binding applied to the writer.
                                 *
                                 * @see groovy.lang.Writable#writeTo(java.io.Writer)
                                 */
                                public Writer writeTo(Writer writer) {
                                        Binding binding;
                                        if (map == null)
                                                binding = new Binding();
                                        else
                                                binding = new Binding(map);

                                        Script scriptObject = InvokerHelper.createScript(script.getClass(), binding);
                                        PrintWriter pw = new PrintWriter(writer);
                                        scriptObject.setProperty("rout", pw);
                                        scriptObject.run();
                                        pw.flush();
                                        return writer;
                                }

                                /**
                                 * Convert the template and binding into a result String.
                                 *
                                 * @see java.lang.Object#toString()
                                 */
                                public String toString() {
                                        StringWriter sw = new StringWriter();
                                        writeTo(sw);
                                        return sw.toString();
                                }
                        };
                }

                /**
                 * Parse the text document looking for <% or <%= and then call out to the appropriate handler, otherwise copy the text directly
                 * into the script while escaping quotes.
                 *
                 * @param reader a reader for the template text
                 * @return the parsed text
                 * @throws IOException if something goes wrong
                 */
                protected String parse(Reader reader, boolean rootTemplate) throws IOException {
                        if (!reader.markSupported()) {
                                reader = new BufferedReader(reader);
                        }
                        StringWriter sw = new StringWriter();
                        if (rootTemplate)
                                startScript(sw);
                        int c;
                        while ((c = reader.read()) != -1) {
                                if (c == '<') {
                                        reader.mark(1);
                                        c = reader.read();
                                        if (c != '%') {
                                                sw.write('<');
                                                reader.reset();

                                        } else {
                                                reader.mark(1);
                                                c = reader.read();
                                                if (c == '=') {
                                                        groovyExpression(reader, sw);
                                                } else if (c == '@') {
                                                        processDirective(reader, sw);

                                                } else {
                                                        reader.reset();
                                                        groovySection(reader, sw);
                                                }
                                        }
                                        continue;// at least '<' is consumed … read next chars.
                                }
                                if (c == '$') {
                                        reader.mark(1);
                                        c = reader.read();
                                        if (c != '{') {
                                                sw.write('$');
                                                reader.reset();
                                        } else {
                                                reader.mark(1);
                                                sw.write("${");
                                                processGSstring(reader, sw);
                                        }
                                        continue;// at least '$' is consumed … read next chars.
                                }
                                if (c == '\"') {
                                        sw.write('\\');
                                }
                                /*
                                * Handle raw new line characters.
                                */
                                if (c == '\n' || c == '\r') {
                                        if (c == '\r') {// on Windows, "\r\n" is a new line.
                                                reader.mark(1);
                                                c = reader.read();
                                                if (c != '\n') {
                                                        reader.reset();
                                                }
                                        }
                                        sw.write("\\n\");\nrout.print(\"");
                                        continue;
                                }
                                sw.write(c);
                        }
                        endScript(sw);
                        return sw.toString();
                }

                private void startScript(StringWriter sw) {
                        //sw.write("/* Generated by SimpleTemplateEngine */\n");
                        sw.write("rout.print(\"");
                }

                private void endScript(StringWriter sw) {
                        sw.write("\");\n");
                }

                private void processDirective(Reader reader, StringWriter sw) throws IOException {
                        int c;
                        StringWriter directiveWriter = new StringWriter();

                        while ((c = reader.read()) != -1) {

                                if (c == '>')
                                        break;

                                if (c != '\n' && c != '\r') {
                                        directiveWriter.write(c);
                                }
                        }

                        String directive = directiveWriter.toString().trim();
                        int i = directive.indexOf(' ');

                        if (directive.substring(0, i).equalsIgnoreCase("include")) {
                                processIncludeDirective(sw, directive.split("'")[1]);
                        }
                }

                private void processIncludeDirective(StringWriter sw, String path) throws IncludeDirectiveException {
                        try {
                                String templateRoot;
                                File f = null;
                                if (path.charAt(0) == '/') {      //root request

                                        if (File.separator.equals("/")) {
                                                f = new File(RequestThreadInfo.get().getApplication().getAppPath() + path.trim());
                                        } else {
                                                f = new File(RequestThreadInfo.get().getApplication().getAppPath() + path.trim().replaceAll("/", "\\\\"));
                                        }
                                        templateRoot = RequestThreadInfo.get().getApplication().getAppPath();
                                } else {
                                        if (File.separator.equals("/")) {
                                                f = new File(RequestThreadInfo.get().getTemplateInfo().getTemplateRoot() + File.separator + path.trim());
                                        } else {
                                                f = new File(RequestThreadInfo.get().getTemplateInfo().getTemplateRoot() + File.separator + path.trim().replaceAll("/", "\\\\"));
                                        }

                                        templateRoot = RequestThreadInfo.get().getTemplateInfo().getTemplateRoot();
                                }

                                List entry = RequestThreadInfo.get().getTemplateInfo().getCache();
                                entry.add(f.getAbsolutePath());

                                log.fine("including file : " + f.getAbsolutePath() + ",  into parent file: " + f.getParentFile().getCanonicalPath());

                                SimpleTemplate template = new SimpleTemplate();
                                String script = null;

                                RequestThreadInfo.get().getTemplateInfo().setTemplateRoot(f.getParentFile().getCanonicalPath());
                                script = template.parse(new FileReader(f), false);
                                RequestThreadInfo.get().getTemplateInfo().setTemplateRoot(templateRoot);

                                sw.write(script);
                                sw.write("rout.print(\"");
                        } catch (IOException e) {
                                throw new IncludeDirectiveException(" error processing include directive for path: " + path, e);
                        }

                }

                private void processGSstring(Reader reader, StringWriter sw) throws IOException {
                        int c;
                        while ((c = reader.read()) != -1) {
                                if (c != '\n' && c != '\r') {
                                        sw.write(c);
                                }
                                if (c == '}') {
                                        break;
                                }
                        }
                }

                /**
                 * Closes the currently open write and writes out the following text as a GString expression until it reaches an end %>.
                 *
                 * @param reader a reader for the template text
                 * @param sw     a StringWriter to write expression content
                 * @throws IOException if something goes wrong
                 */
                private void groovyExpression(Reader reader, StringWriter sw) throws IOException {
                        sw.write("\");rout.print(\"${");
                        int c;
                        while ((c = reader.read()) != -1) {
                                if (c == '%') {
                                        c = reader.read();
                                        if (c != '>') {
                                                sw.write('%');
                                        } else {
                                                break;
                                        }
                                }
                                if (c != '\n' && c != '\r') {
                                        sw.write(c);
                                }
                        }
                        sw.write("}\");\nrout.print(\"");
                }

                /**
                 * Closes the currently open write and writes the following text as normal Groovy script code until it reaches an end %>.
                 *
                 * @param reader a reader for the template text
                 * @param sw     a StringWriter to write expression content
                 * @throws IOException if something goes wrong
                 */
                private void groovySection(Reader reader, StringWriter sw) throws IOException {
                        sw.write("\");");
                        int c;
                        while ((c = reader.read()) != -1) {
                                if (c == '%') {
                                        c = reader.read();
                                        if (c != '>') {
                                                sw.write('%');
                                        } else {
                                                break;
                                        }
                                }
                                /* Don't eat EOL chars in sections - as they are valid instruction separators.
                                * See http://jira.codehaus.org/browse/GROOVY-980
                                */
                                // if (c != '\n' && c != '\r') {
                                sw.write(c);
                                //}
                        }
                        sw.write(";\nrout.print(\"");
                }
        }

}