package com.sybrix.easygsp.http;

import java.io.File;

/**
 * ErrorStatus <br/>
 *
 * @author David Lee
 */
public class RequestInfo {
        private String currentFile;
        private Application application;
        private boolean templateRequest;
        private boolean scriptProcessed;
        private String uniqueScriptName;
        private String realScriptName;
        private TemplateInfo templateInfo;
        private ParsedRequest parsedRequest;

        public RequestInfo() {
                templateInfo = new TemplateInfo();
        }

        public String getCurrentFile() {
                if (!isTemplateRequest() && isScriptProcessed()) {
                        return currentFile;
                } else if (!isTemplateRequest()) {
                        return currentFile.replace(RequestThread.altExtension,RequestThread.groovyExtension);
                } else {
                        return uniqueScriptName + RequestThread.groovyExtension;
                }
                //return currentFile;
        }

        public void setCurrentFile(String currentFile) {
                this.currentFile = currentFile;
        }

        public Application getApplication() {
                return application;
        }

        public void setApplication(Application application) {
                this.application = application;
        }

        public boolean isTemplateRequest() {
                return templateRequest;
        }

        public void setTemplateRequest(boolean templateRequest) {
                this.templateRequest = templateRequest;
        }

        public boolean isScriptProcessed() {
                return scriptProcessed;
        }

        public void setScriptProcessed(boolean scriptProcessed) {
                this.scriptProcessed = scriptProcessed;
        }

        public String getUniqueScriptName() {
                return uniqueScriptName;
        }

        public void setUniqueScriptName(String uniqueScriptName) {
                this.uniqueScriptName = uniqueScriptName;
        }

        public String getRealScriptName() {
                return realScriptName;
        }

        public void setRealScriptName(String realScriptName) {
                this.realScriptName = realScriptName;
        }

        public TemplateInfo getTemplateInfo() {
                return templateInfo;
        }

        public void setTemplateInfo(TemplateInfo templateInfo) {
                this.templateInfo = templateInfo;
        }

        public ParsedRequest getParsedRequest() {
                return parsedRequest;
        }

        public void setParsedRequest(ParsedRequest parsedRequest) {
                this.parsedRequest = parsedRequest;
        }
}