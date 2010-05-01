package com.sybrix.easygsp.http;

import java.io.Serializable;

/**
 * SessionMessage <br/>
 * The message sent between JGroups instances for the purpose of maintaining state
 *
 * @author David Lee
 */
public class ClusterMessage implements Serializable {
        private transient String method;
        private String sessionId;
        private transient String appId;
        private Object[] parameters;
        private transient String appPath;

        public ClusterMessage(String method, Object[] parameters) {
                this.method = method;
                this.parameters = parameters;
        }

        public ClusterMessage( String appId,String appPath, String sessionId, String method, Object[] parameters) {
                this.method = method;
                this.sessionId = sessionId;
                this.appId = appId;
                this.parameters = parameters;
                this.appPath = appPath; 
        }

        public String getMethod() {
                return method;
        }
        public void setMethod(String method) {
                this.method = method;
        }
        public String getSessionId() {
                return sessionId;
        }
        public void setSessionId(String sessionId) {
                this.sessionId = sessionId;
        }
        public String getAppId() {
                return appId;
        }
        public void setAppId(String appId) {
                this.appId = appId;
        }
        public Object[] getParameters() {
                return parameters;
        }
        public void setParameters(Object[] parameters) {
                this.parameters = parameters;
        }

        public String getAppPath() {
                return appPath;
        }
        public void setAppPath(String appPath) {
                this.appPath = appPath;
        }

        @Override
        public String toString() {
                return "method: " + method + ", app: " + appId + ", sessionId: " + sessionId;
        }
}
