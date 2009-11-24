package com.sybrix.easygsp.http;

/**
 * ParsedRequest <br/>
 * Description :
 */
public class ParsedRequest {
        protected String appName;
        protected String appPath;
        protected String requestURI;
        protected String requestFilePath;

        public ParsedRequest() {
        }

        public ParsedRequest(String appName, String appPath, String requestFilePath, String requestURI) {
                this.appName = appName;
                this.appPath = appPath;
                this.requestFilePath = requestFilePath;
                this.requestURI = requestURI;
        }

        public String getAppName() {
                return appName;
        }

        public String getAppPath() {
                return appPath;
        }

        public String getRequestURI() {
                return requestURI;
        }

        public void setAppName(String appName) {
                this.appName = appName;
        }

        public void setAppPath(String appPath) {
                this.appPath = appPath;
        }

        public void setRequestURI(String requestURI) {
                this.requestURI = requestURI;
        }

        public String getRequestFilePath() {
                return requestFilePath;
        }

        public void setRequestFilePath(String requestFilePath) {
                this.requestFilePath = requestFilePath;
        }
}
