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

import org.jgroups.util.Streamable;
import org.jgroups.Header;

import java.io.*;

/**
 * AppHeader <br/>
 *
 * @author David Lee
 */

public class AppHeader extends Header implements Streamable {
        public String appName = null;
        int size = 0;

        public AppHeader() {
        }  // used for externalization

        public AppHeader(String appName, String method, String appPath) {
                this.appName = appName + ";" + method + ";" + appPath;
                if (this.appName != null)
                        size = this.appName.length() + 2; // +2 for writeUTF()
        }

        public String toString() {
                return "[application=" + appName + "]";
        }


        public int size() {
                return size;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
                out.writeUTF(appName);
        }


        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                appName = in.readUTF();
        }


        public void writeTo(DataOutputStream out) throws IOException {
                out.writeUTF(appName);
        }

        public void readFrom(DataInputStream in) throws IOException, IllegalAccessException, InstantiationException {
                appName = in.readUTF();
                if (appName != null)
                        size = appName.length() + 2; // +2 for writeUTF()
        }
}

