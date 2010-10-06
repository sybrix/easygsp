package com.sybrix.easygsp.http;

/**
 * ErrorStatus <br/>
 *
 * @author David Lee
 */
public class RequestInfo {
        private String currentFile;
        private ServletContextImpl application;
        private Boolean templateRequest=false;
        private Boolean scriptProcessed=false;
        private String uniqueTemplateScriptName;
        private String realScriptName;
        private TemplateInfo templateInfo;
        private ParsedRequest parsedRequest;
        private Boolean errorOccurred = false;
        private CustomServletBinding binding;
        private Boolean codeBehindChanged = false;
        private Integer forwardCount = 0;
        private RequestError requestError;
        private RequestImpl requestImpl;


        public RequestInfo() {
                templateInfo = new TemplateInfo();
                requestError = new RequestError();
        }

        public String getCurrentFile() {
                return currentFile;
//                if (!isTemplateRequest() && isScriptProcessed()) {
//                        return currentFile;
//                } else if (!isTemplateRequest()) {
//                        return currentFile.replace(RequestThread.altExtension,RequestThread.groovyExtension);
//                } else if (isTemplateRequest()) {
//
//                } else {
//                        return uniqueTemplateScriptName + RequestThread.groovyExtension;
//                }
//                //return currentFile;
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

        public String getUniqueTemplateScriptName() {
                return uniqueTemplateScriptName;
        }

        public void setUniqueTemplateScriptName(String uniqueTemplateScriptName) {
                this.uniqueTemplateScriptName = uniqueTemplateScriptName;
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

        public boolean isCodeBehindNewer() {
                return codeBehindChanged;
        }

        public void setCodeBehindChanged(boolean codeBehindChanged) {
                this.codeBehindChanged = codeBehindChanged;
        }

        public void increaseForwardCount() {
                forwardCount++;
        }

        public int getForwardCount() {
                return forwardCount;
        }

        public RequestError getRequestError() {
                return requestError;
        }

        public void setRequestError(RequestError requestError) {
                this.requestError = requestError;
        }

        public RequestImpl getRequestImpl() {
                return requestImpl;
        }

        public void setRequestImpl(RequestImpl requestImpl) {
                this.requestImpl = requestImpl;
        }

        protected void clear() {
                currentFile = null;
                application = null;
                templateRequest = null;
                scriptProcessed = null;
                uniqueTemplateScriptName = null;
                realScriptName = null;
                templateInfo = null;
                parsedRequest = null;
                errorOccurred = null;
                binding = null;
                codeBehindChanged = null;
                forwardCount = null;
                requestError = null;
                requestImpl = null;
        }

}
