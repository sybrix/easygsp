package com.sybrix.easygsp.http;

/**
 * ErrorStatus <br/>
 *
 * @author David Lee
 */
public class RequestInfo {
        private String currentFile;
        private ServletContextImpl application;
        private boolean templateRequest;
        private boolean scriptProcessed;
        private String uniqueScriptName;
        private String realScriptName;
        private TemplateInfo templateInfo;
        private ParsedRequest parsedRequest;
        private boolean errorOccurred = false;
        private CustomServletBinding binding;

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

        public ServletContextImpl getApplication() {
                return application;
        }

        public void setApplication(ServletContextImpl application) {
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

        public boolean errorOccurred() {
                return errorOccurred;
        }

        public void setErrorOccurred(boolean errorOccurred) {
                this.errorOccurred = errorOccurred;
        }

        public CustomServletBinding getBinding() {
                return binding;
        }

        public void setBinding(CustomServletBinding binding) {
                this.binding = binding;
        }
}
