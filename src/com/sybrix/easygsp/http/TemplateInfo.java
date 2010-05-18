package com.sybrix.easygsp.http;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * TemplateInfo <br/>
 *
 * @author David Lee
 */
public class TemplateInfo {
        private String templateRoot;
        private Boolean sourceNewer = false;
        TemplateServlet.TemplateCacheEntry cachEntry;
        private Set children;

        public TemplateInfo() {
                children = new HashSet();
        }
        public TemplateInfo(String templateRoot) {
                this();
                this.templateRoot = templateRoot;
        }

        public String getTemplateRoot() {
                return templateRoot;
        }

        public void setTemplateRoot(String templateRoot) {
                this.templateRoot = templateRoot;
        }

        public Boolean isSourceNewer() {
                return sourceNewer;
        }

        public void setSourceNewer(Boolean sourceNewer) {
                this.sourceNewer = sourceNewer;
        }
        
        public Set getChildren() {
                return children;
        }
        public void setChildren(Set children) {
                this.children = children;
        }
}
