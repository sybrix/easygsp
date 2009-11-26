package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.File;

/**
 * Parsers <br/>
 * Description :
 */
public class Parsers {
        private static final Logger log = Logger.getLogger(Parsers.class.getName());

        private static boolean isVirtualHostingEnabled = false;
        private static List defaultVirtualHostList = new ArrayList();

        static {
                isVirtualHostingEnabled = EasyGServer.propertiesFile.getBoolean("virtual.hosting", false);
                String[] hosts = EasyGServer.propertiesFile.getString("default.host").split(",");
                for (String host : hosts) {
                        defaultVirtualHostList.add(host);
                }

        }

        public static class ApacheParser extends PathParser {

                public static ParsedRequest parseRequestFromHeader(Map<String, String> headers) {
                        ParsedRequest parsedRequest = new ParsedRequest();

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
                                appName = webAppDir.substring(webAppDir.lastIndexOf('/')+1, webAppDir.length());
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

                                path = scriptName.substring(scgiMount.length() + 1);
                                appName = path.substring(0, path.substring(1).indexOf('/') + 1);
                                requestURI = scriptName.substring(scgiMount.length() + appName.length() + 2);
                                requestURIPath = webAppDir + "/" + path;
                                path = webAppDir + "/" + appName;
                        }
                        log.fine("parsed AppName: " + appName);
                        log.fine("parsed AppDir: " + path);
                        log.fine("parsed RequestURIPath: " + requestURIPath);
                        log.fine("parsed AppName: " + appName);

                        parsedRequest.setAppName(appName);
                        parsedRequest.setAppPath(path);
                        parsedRequest.setRequestFilePath(requestURIPath);
                        parsedRequest.setRequestURI(requestURI);

                        return parsedRequest;
                }
        }

        private static String parseFirstFolderName(String path){
                return path.substring(1,path.lastIndexOf('/'));
        }

        private static String parseLastFolderName(String path){
                   int lengthWithoutLastSlash = path.length() - 1;
                   int lastSlashIndex = path.substring(0, lengthWithoutLastSlash).lastIndexOf('/');

                return path.substring(lastSlashIndex,lengthWithoutLastSlash);
        }

        public static class LightyParser extends PathParser {

                public static ParsedRequest parseRequestFromHeader(Map<String, String> headers) {
                        ParsedRequest parsedRequest = new ParsedRequest();

                        String webAppDir = headers.get(RequestHeaders.DOCUMENT_ROOT);
                        String scriptFileName = headers.get(RequestHeaders.SCRIPT_FILENAME);
                        String scriptName = headers.get(RequestHeaders.SCRIPT_NAME);
                        String hostName = headers.get(RequestHeaders.SERVER_NAME);
                        String appName = null;
                        String requestURIPath = null;
                        String requestURI = null;

                        //for simple vhost & mysql-vhost, DOCUMENT_ROOT ends with slash
                        if (isVirtualHostingEnabled && !defaultVirtualHostList.contains(hostName)) {
                                // simple vhost - server-root + hostname + document-root
                                int lengthWithoutLastSlash = webAppDir.length() - 1;
                                int lastSlashIndex = webAppDir.substring(0, lengthWithoutLastSlash).lastIndexOf('/');

                                appName = webAppDir.substring(lastSlashIndex + 1, lengthWithoutLastSlash);
                                requestURIPath = scriptFileName;
                                requestURI = scriptFileName.substring(webAppDir.length());
                                webAppDir = webAppDir.substring(0, webAppDir.length() - 1);
                        } else if (isVirtualHostingEnabled) {
                                // simple vhost - server-root + hostname + document-root
                                int secondSlashIndex = scriptName.substring(1).indexOf('/');
                                String appFolderName = scriptName.substring(1, secondSlashIndex + 1);

                                appName = appFolderName;
                                requestURIPath = scriptFileName;
                                requestURI = scriptName.substring(appFolderName.length() + 2, scriptName.length());
                                webAppDir = webAppDir + appFolderName;

                        } else {
                                int secondSlashIndex = scriptName.substring(1).indexOf('/');

                                appName = scriptName.substring(1, secondSlashIndex + 1);
                                requestURIPath = scriptFileName;
                                requestURI = scriptName.substring(appName.length() + 2);
                                webAppDir = webAppDir + "/" + appName;
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


                        parsedRequest.setAppName(appName);
                        parsedRequest.setAppPath(webAppDir);
                        parsedRequest.setRequestURI(requestURI);
                        parsedRequest.setRequestFilePath(requestURIPath);

                        log.fine("parsed AppName: " + appName);
                        log.fine("parsed AppDir: " + parsedRequest.getAppPath());
                        log.fine("parsed RequestURIPath: " + requestURIPath);
                        log.fine("parsed RequestURI: " + requestURI);


                        return parsedRequest;
                }
        }

}
