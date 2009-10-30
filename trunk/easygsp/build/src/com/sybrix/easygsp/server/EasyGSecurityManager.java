/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sybrix.easygsp.server;

import com.sybrix.easygsp.http.Application;
import com.sybrix.easygsp.http.ThreadAppIdentifier;

import java.io.FileDescriptor;

/**
 * SCGISecurityManager <br/>
 * Description :
 */
public class EasyGSecurityManager extends SecurityManager {
        private static boolean allowAWT;
        private static boolean allowSwing;

        static {
                allowAWT = EasyGServer.propertiesFile.getBoolean("allow.awt");
                allowSwing = EasyGServer.propertiesFile.getBoolean("allow.swing");
        }
        
        public void checkDelete(String file) {
                super.checkDelete(file);
        }

        public void checkRead(FileDescriptor fd) {
                super.checkRead(fd);
        }

        public void checkRead(String file) {
                doCheckReadOrWrite(file);
        }

        private void doCheckReadOrWrite(String file) {
                if (file.contains("AppId"))
                        return;

                Application path = ThreadAppIdentifier.get();
                if (path == null) {
                        super.checkRead(file);
                } else {
                        boolean b = file.startsWith(path.getAppPath());
                        if (b) {
                                return;
                        } else {
                                try {
                                        super.checkRead(file);
                                } catch (SecurityException e) {
                                        throw e;
                                }
                        }
                }
        }

        public void checkRead(String file, Object context) {
                super.checkRead(file, context);
        }

        public void checkWrite(FileDescriptor fd) {
                super.checkWrite(fd);
        }

        public void checkWrite(String file) {
                doCheckReadOrWrite(file);
        }

        public void checkExit(int status) {
                Application path = ThreadAppIdentifier.get();
                if (path != null) {
                        throw new SecurityException("System.exit() access denied.");
                } else {
                        super.checkExit(status);
                }
        }

        public void checkAccess(ThreadGroup g) {
                super.checkAccess(g);
        }

        public void checkAccess(Thread t) {
                super.checkAccess(t);
        }

        public void checkPackageAccess(String pkg) {
                Application path = ThreadAppIdentifier.get();
                if (path != null) {
                        if ((pkg.equals("javax.swing") && !allowSwing) || (pkg.equals("java.awt") && allowAWT)) {
                                throw new SecurityException("Access denied for package: " + pkg);
                        }
                }
                super.checkPackageAccess(pkg);
        }
}
