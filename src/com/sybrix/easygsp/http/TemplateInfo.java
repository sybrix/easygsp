package com.sybrix.easygsp.http;

import java.util.List;
import java.util.ArrayList;

/**
 * TemplateInfo <br/>
 *
 * @author David Lee
 */
public class TemplateInfo {
        private String templateRoot;
        private List cache;
        private Boolean sourceNewer = false;

        public TemplateInfo() {
                cache = new ArrayList();
        }

        public TemplateInfo(String templateRoot, List cache) {
                this.templateRoot = templateRoot;
                this.cache = cache;
        }

        public String getTemplateRoot() {
                return templateRoot;
        }

        public void setTemplateRoot(String templateRoot) {
                this.templateRoot = templateRoot;
        }

        public List getCache() {
                return cache;
        }

        public void setCache(List cache) {
                this.cache = cache;
        }

        public Boolean isSourceNewer() {
                return sourceNewer;
        }

        public void setSourceNewer(Boolean sourceNewer) {
                this.sourceNewer = sourceNewer;
        }
}
