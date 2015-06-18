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
