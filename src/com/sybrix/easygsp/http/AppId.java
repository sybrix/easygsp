package com.sybrix.easygsp.http;

import java.io.Serializable;

/**
 * AppId <br/>
 *
 * @author David Lee
 */
public class AppId implements Serializable {
        private String appName;
        private String appPath;

        public AppId(String appName, String appPath) {
                this.appName = appName;
                this.appPath = appPath;
        }

        public String getAppName() {
                return appName;
        }
        public void setAppName(String appName) {
                this.appName = appName;
        }
        public String getAppPath() {
                return appPath;
        }
        public void setAppPath(String appPath) {
                this.appPath = appPath;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                AppId appId = (AppId) o;

                if (appName != null ? !appName.equals(appId.appName) : appId.appName != null) return false;
                if (appPath != null ? !appPath.equals(appId.appPath) : appId.appPath != null) return false;

                return true;
        }
        @Override
        public int hashCode() {
                int result = appName != null ? appName.hashCode() : 0;
                result = 31 * result + (appPath != null ? appPath.hashCode() : 0);
                return result;
        }
}
