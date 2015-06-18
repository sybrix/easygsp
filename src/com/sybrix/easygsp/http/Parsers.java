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

import com.sybrix.easygsp.server.EasyGServer;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.File;

/**
 * Parsers <br/>
 * Description :
 */
public class Parsers {
        private static final Logger logger = Logger.getLogger(Parsers.class.getName());

        private static boolean isVirtualHostingEnabled = false;
        private static List defaultVirtualHostList = new ArrayList();

        static {
                isVirtualHostingEnabled = EasyGServer.propertiesFile.getBoolean("virtual.hosting", false);
                String[] hosts = EasyGServer.propertiesFile.getString("default.host", "localhost").split(",");
                for (String host : hosts) {
                        defaultVirtualHostList.add(host);
                }

        }

        public static class ApacheParser extends PathParser {

                public static ParsedRequest parseRequestFromHeader(Map<String, String> headers) {


                        String webAppDir = headers.get(RequestHeaders.DOCUMENT_ROOT);
                        String scgiMount = headers.get(RequestHeaders.SCRIPT_NAME);
                        String hostName = headers.get(RequestHeaders.SERVER_NAME);

                        String appName = null;
                        String path = null;
                        String requestURI = null;
                        String requestURIPath = null;

//                        if (!scriptName.endsWith(RequestThread.altExtension) && !scriptName.endsWith(RequestThread.groovyExtension) && !scriptName.endsWith(RequestThread.templateExtension)) {
//
//                        }
                        if (isVirtualHostingEnabled && !defaultVirtualHostList.contains(hostName)) {
                                String scriptName = headers.get(RequestHeaders.PATH_INFO);
                                appName = webAppDir.substring(webAppDir.lastIndexOf('/') + 1, webAppDir.length());
                                requestURI = scriptName.substring(1);
                                requestURIPath = webAppDir + scriptName;
                                path = webAppDir;
                        } else if (isVirtualHostingEnabled) {

                                String scriptName = headers.get(RequestHeaders.PATH_INFO);
                                appName = parseFirstFolderName(scriptName);
                                requestURIPath = webAppDir + scriptName;

                                webAppDir = webAppDir + "/" + appName;
                                requestURI = scriptName.substring(appName.length() + 2);

                                path = webAppDir;

                        } else {
                                String scriptName = headers.get(RequestHeaders.SCRIPT_NAME) + headers.get(RequestHeaders.PATH_INFO);
                                if (!(scriptName.endsWith(".gspx") || scriptName.endsWith(".gsp")))
                                        if (!scriptName.endsWith("/")) {
                                                scriptName += "/";

                                        }
                                path = scriptName.substring(scgiMount.length() + 1);

                                appName = path.substring(0, path.substring(1).indexOf('/') + 1);
                                requestURI = scriptName.substring(scgiMount.length() + appName.length() + 2);
                                requestURIPath = webAppDir + "/" + path;
                                if (requestURIPath.endsWith("/")) {
                                        if (new File(requestURIPath + "index.gspx").exists()) {
                                                requestURIPath = requestURIPath + "index.gspx";
                                                requestURI = "index.gspx";
                                        } else {
                                                requestURIPath = requestURIPath + "index.gsp";
                                                requestURI = "index.gsp";
                                        }
                                }
                                path = webAppDir + "/" + appName;
                        }

                        ParsedRequest parsedRequest = new ParsedRequest(appName, path, requestURIPath, requestURI);

                        logger.fine("parsed AppName: " + appName);
                        logger.fine("parsed AppDir: " + parsedRequest.getAppPath());
                        logger.fine("parsed RequestURIPath: " + requestURIPath);
                        logger.fine("parsed RequestURI: " + requestURI);

                        return parsedRequest;
                }
        }

        private static String parseFirstFolderName(String path) {
                return path.substring(1, path.lastIndexOf('/'));
        }

        private static String parseLastFolderName(String path) {
                int lengthWithoutLastSlash = path.length() - 1;
                int lastSlashIndex = path.substring(0, lengthWithoutLastSlash).lastIndexOf('/');

                return path.substring(lastSlashIndex, lengthWithoutLastSlash);
        }

        public static class LightyParser extends PathParser {

                public static ParsedRequest parseRequestFromHeader(Map<String, String> headers) {


                        String webAppDir = headers.get(RequestHeaders.DOCUMENT_ROOT);
                        String scriptFileName = headers.get(RequestHeaders.SCRIPT_FILENAME);
                        String scriptName = headers.get(RequestHeaders.SCRIPT_NAME);
                        String hostName = headers.get(RequestHeaders.SERVER_NAME);
                        String appName = null;
                        String requestURIPath = null;
                        String requestURI = null;

                        // determine if the request is a index request
                        // determine if the request is a route request
                        // determine if the request is a normal gspx/gsp request

                        //for simple vhost & mysql-vhost, DOCUMENT_ROOT ends with slash
                        if (isVirtualHostingEnabled && !defaultVirtualHostList.contains(hostName)) {
                                // simple vhost - server-root + hostname + document-root
                                int lengthWithoutLastSlash = webAppDir.length() - 1;
                                int lastSlashIndex = webAppDir.substring(0, lengthWithoutLastSlash).lastIndexOf('/');

                                appName = webAppDir.substring(lastSlashIndex + 1, lengthWithoutLastSlash);

                                if (EasyGServer.routingEnabled) {

                                        String uri = extractRequestUri(headers, false);

                                        if (!ParsedRequest.gspExtensionFound(uri)) {
                                                //!ParsedRequest.gspExtensionFound(headers.get("REQUEST-URI"))) {
                                                requestURI = removeLastSlash(uri);
                                                requestURIPath = webAppDir + requestURI;
                                                webAppDir = webAppDir.substring(0, webAppDir.length() - 1);

                                        } else {


                                                requestURI = uri;
                                                requestURIPath = webAppDir + uri;
                                                //webAppDir = requestURIPath;
                                                //requestURIPath += scriptName;
                                        }

                                } else {
                                        requestURIPath = scriptFileName;
                                        requestURI = scriptFileName.substring(webAppDir.length());
                                        webAppDir = webAppDir.substring(0, webAppDir.length() - 1);
                                }


                        } else if (isVirtualHostingEnabled) {   // this assumes that the default host has many apps
                                if (EasyGServer.routingEnabled) {
                                        String originalUri = headers.get("REQUEST-URI");
                                        String uri = extractRequestUri(headers, true);

                                        requestURI = removeLastSlash(uri);

                                        int c = originalUri.indexOf('/', 1);
                                        if (c == -1) {
                                                appName = originalUri.substring(1);
                                        } else {
                                                appName = originalUri.substring(1, c);
                                        }

                                        webAppDir = removeLastSlash(webAppDir) + "/" + appName;

                                        if (!ParsedRequest.gspExtensionFound(uri)) {
                                                requestURIPath = webAppDir + "/" + appName + "/" + requestURI;
                                        } else {
                                                requestURIPath = webAppDir + "/" + requestURI;
                                        }
                                } else {
                                        // simple vhost - server-root + hostname + document-root
                                        int secondSlashIndex = scriptName.substring(1).indexOf('/');
                                        String appFolderName = scriptName.substring(1, secondSlashIndex + 1);

                                        appName = appFolderName;
                                        requestURIPath = scriptFileName;
                                        requestURI = scriptName.substring(appFolderName.length() + 2, scriptName.length());
                                        webAppDir = webAppDir + appFolderName;
                                }

                        } else {
                                if (EasyGServer.routingEnabled) {
                                        String originalUri = headers.get("REQUEST-URI");
                                        String uri = extractRequestUri(headers, true);

                                        requestURI = removeLastSlash(uri);

                                        int c = originalUri.indexOf('/', 1);
                                        if (c == -1) {
                                                appName = originalUri.substring(1);
                                        } else {
                                                appName = originalUri.substring(1, c);
                                        }

                                        webAppDir = removeLastSlash(webAppDir) + "/" + appName;

                                        requestURIPath = webAppDir + "/" + requestURI;

                                } else {
                                        int secondSlashIndex = scriptName.substring(1).indexOf('/');

                                        appName = scriptName.substring(1, secondSlashIndex + 1);
                                        requestURIPath = scriptFileName;
                                        requestURI = scriptName.substring(appName.length() + 2);
                                        webAppDir = webAppDir + "/" + appName;
                                }
                        }

//                        if (!scriptName.endsWith(RequestThread.altExtension) && !scriptName.endsWith(RequestThread.groovyExtension) && !scriptName.endsWith(RequestThread.templateExtension)) {
//                                if (headers.get(RequestHeaders.SCRIPT_NAME).endsWith("/")) {
//                                        headers.put(RequestHeaders.SCRIPT_NAME, headers.get(RequestHeaders.SCRIPT_NAME) + "index" + RequestThread.defaultExtension);
//                                } else {
//                                        headers.put(RequestHeaders.SCRIPT_NAME, headers.get(RequestHeaders.SCRIPT_NAME) + "/index" + RequestThread.defaultExtension);
//                                }
//                        }

//
//                        String path = null;
//
//                        if (scriptName.substring(1).indexOf('/') >= 0) {
//                                appName = scriptName.substring(1, scriptName.indexOf('/', 1));
//                                requestURI = scriptName.substring(appName.length() + 2, scriptName.length());
//                                requestURIPath = webAppDir + scriptName;
//                        } else {
//                                if (!webAppDir.endsWith("/"))
//                                        appName = webAppDir.substring(webAppDir.lastIndexOf('/'));
//                                else {
//                                        int lastSlash = webAppDir.length() - 1;
//                                        int secondToLastSlash = webAppDir.substring(0, lastSlash).lastIndexOf('/');
//                                        appName = webAppDir.substring(secondToLastSlash + 1, lastSlash);
//                                }
//                        }

                        ParsedRequest parsedRequest = new ParsedRequest(appName, webAppDir, requestURIPath, requestURI);
//                        if (EasyGServer.routingEnabled && !ParsedRequest.gspExtensionFound(headers.get("REQUEST-URI"))) {
//                                parsedRequest.setRequestURI(headers.get("REQUEST-URI"));
//                                parsedRequest.setRequestFilePath(webAppDir);
//                        }

                        logger.fine("parsed AppName: " + appName);
                        logger.fine("parsed AppDir: " + parsedRequest.getAppPath());
                        logger.fine("parsed RequestURIPath: " + requestURIPath);
                        logger.fine("parsed RequestURI: " + requestURI);

                        return parsedRequest;
                }

        }

        public static String extractRequestUri(Map<String, String> headers, boolean removeDefaultApp) {
                int c = headers.get("REQUEST-URI").indexOf('?');
                String uri = c >= 0 ? headers.get("REQUEST-URI").substring(1, c) : headers.get("REQUEST-URI").substring(1);

                if (removeDefaultApp) {
                        c = uri.indexOf('/');
                        return c == -1 ? "" : uri.substring(c + 1, uri.length());
                } else {
                        return uri;
                }
        }


        public static String removeLastSlash(String s) {

                if (s.length() > 0 && s.charAt(s.length() - 1) == '/') {
                        return s.substring(0, s.length() - 1);
                } else {
                        return s;
                }
        }
}
