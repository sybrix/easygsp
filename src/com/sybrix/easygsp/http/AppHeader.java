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

