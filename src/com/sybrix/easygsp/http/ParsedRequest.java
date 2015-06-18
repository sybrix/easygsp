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

import com.sybrix.easygsp.exception.IndexFileNotFoundException;
import com.sybrix.easygsp.server.EasyGServer;
import com.sybrix.easygsp.util.StringUtil;

import java.io.File;

/**
 * ParsedRequest <br/>
 * Description :
 */
public class ParsedRequest {
        protected String appName;
        protected String appPath;
        protected String requestURI;
        protected String requestFilePath;

        static String ext1;
        static String ext2;
        static String ext3;
        protected boolean extensionFound = false;

        static {
                ext1 = EasyGServer.propertiesFile.getString("template.extension");
                ext2 = EasyGServer.propertiesFile.getString("view.extension");
                ext3 = EasyGServer.propertiesFile.getString("alt.groovy.extension");
        }

        public ParsedRequest(String appName, String appPath, String requestFilePath, String requestURI) {
                this.appName = appName;
                this.appPath = appPath;
                this.requestFilePath = requestFilePath;
                this.requestURI = requestURI;

                //extensionFound = gspExtensionFound(requestURI);
        }

        public static boolean gspExtensionFound(String requestURI) {
                if (requestURI.endsWith(ext1) || requestURI.endsWith(ext3) || requestURI.endsWith(ext2)) {
                        return true;
                } else {
                        return false;
                }
        }

        public void indexCheck() throws IndexFileNotFoundException {
                if (!gspExtensionFound(requestURI)) {
                        String files[] = EasyGServer.propertiesFile.getString("index.files", "").split(",");
                        for (String file : files) {
                                file = file.trim();

                                File fileObj = new File(this.requestURI.equals("") ?
                                        appPath + "/" + file :
                                        appPath + "/" + this.requestURI + "/" + file
                                );

                                if (fileObj.exists()) {
                                        String fullPath = fileObj.getAbsolutePath().replaceAll("\\\\","/");
                                        this.requestURI = fullPath.substring(appPath.length()+1);
                                        this.requestFilePath = fileObj.getAbsolutePath();
                                        return;
                                }
                        }

                        throw new IndexFileNotFoundException("Index file not found. request uri:" + requestURI);
                }
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

        public boolean isExtensionFound() {
                return extensionFound;
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
