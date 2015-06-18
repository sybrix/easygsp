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
        private TemplateCacheEntry cachEntry;

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
        
        public Set<String> getChildren() {
                return children;
        }
        public void setChildren(Set children) {
                this.children = children;
        }
}
