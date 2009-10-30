package com.sybrix.easygsp.http;

/**
 * TemplateTL <br/>
 *
 * @author David Lee
 */
public class TemplateTL {
        private static final ThreadLocal<TemplateInfo> _id = new ThreadLocal<TemplateInfo>() {
                protected TemplateInfo initialValue() {
                        return new TemplateInfo();
                }
        };

        public static TemplateInfo get() {
                return _id.get();
        }

        protected static void set(TemplateInfo id) {
                _id.set(id);
        }
}
