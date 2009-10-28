package com.sybrix.easygsp.http;

import groovy.lang.GroovyObject;
import groovy.lang.Binding;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CustomServletCategory  {

        public static Object get(ServletContext context, String key) {
                return context.getAttribute(key);
        }

        public static Object get(HttpSession session, String key) {
                return session.getAttribute(key);
        }

        public static Object get(ServletRequest request, String key) {
                return request.getAttribute(key);
        }

        public static Object getAt(ServletContext context, String key) {
                return context.getAttribute(key);
        }

        public static Object getAt(HttpSession session, String key) {
                return session.getAttribute(key);
        }

        public static Object getAt(ServletRequest request, String key) {
                return request.getAttribute(key);
        }

        public static void set(ServletContext context, String key, Object value) {
                context.setAttribute(key, value);
        }

        public static void set(HttpSession session, String key, Object value) {
                session.setAttribute(key, value);
        }

        public static void set(ServletRequest request, String key, Object value) {
                request.setAttribute(key, value);
        }

        public static void putAt(ServletContext context, String key, Object value) {
                context.setAttribute(key, value);
        }

        public static void putAt(HttpSession session, String key, Object value) {
                session.setAttribute(key, value);
        }

        public static void putAt(ServletRequest request, String key, Object value) {
                request.setAttribute(key, value);
        }

        public static void redirect(GroovyObject self, String s) throws IOException {
                Binding binding = (Binding)self.getProperty("binding");
                ResponseImpl response = (ResponseImpl)binding.getVariable("response");
                RequestImpl request = (RequestImpl)binding.getVariable("request");
                request.setAttribute("_explicitForward", true); // when true, controllers don't auto forward
                response.sendRedirect(s);
        }

        public static void forward(GroovyObject self, String s) throws IOException, ServletException {
                Binding binding = (Binding)self.getProperty("binding");
                RequestImpl request = (RequestImpl)binding.getVariable("request");
                request.setAttribute("_explicitForward", true);

                request.forward(s);
        }


        public static void bind(GroovyObject self, String name, Object val) throws IOException, ServletException {
                Binding binding = (Binding)self.getProperty("binding");
                binding.setVariable(name, val);
        }

        public static Integer toInt(GroovyObject self, String val) throws IOException, ServletException {
                if (val == null)
                        return null;
                
                return Integer.parseInt(val);
        }

        public static Double toDbl(GroovyObject self, String val) throws IOException, ServletException {
                return Double.parseDouble(val);
        }

        public static Long toLong(GroovyObject self, String val) throws IOException, ServletException {
                return Long.parseLong(val);
        }

//        public static void log(GroovyObject self, String s) {
//                System.out.println(s);
//        }
//
//        public static void log(GroovyObject self, Throwable t, String s) {
//                System.out.println(s);
//        }

}
