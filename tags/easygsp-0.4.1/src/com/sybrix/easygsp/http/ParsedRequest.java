package com.sybrix.easygsp.http;

import com.sybrix.easygsp.util.StringUtil;

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
                this.requestFilePath = StringUtil.capDriveLetter(requestFilePath);
        }

        public String getControllerClass() {
                String[] s = requestURI.split("/");
                if (s.length == 1) {
                        return StringUtil.capFirstLetter(s[0].substring(0, s[0].lastIndexOf('.')));
                } else {
                        StringBuffer path = new StringBuffer();
                        for (int i = 0; i < s.length; i++) {
                                if (i == (s.length - 1)) {
                                        String cls = s[i].substring(0, s[i].lastIndexOf('.'));
                                        path.append(StringUtil.capFirstLetter(cls));
                                        return path.toString();
                                } else {
                                        path.append(s[i]).append(".");
                                }
                        }

                }
                return null;
        }
}
