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

import com.sybrix.easygsp.http.ServletContextImpl;
import com.sybrix.easygsp.http.RequestThreadInfo;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * SCGISecurityManager <br/>
 * Description :
 */
public class EasyGSecurityManager extends SecurityManager {
        private static boolean allowAWT;
        private static boolean allowSwing;

        static {
                allowAWT = EasyGServer.propertiesFile.getBoolean("allow.awt", false);
                allowSwing = EasyGServer.propertiesFile.getBoolean("allow.swing", false);
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

                ServletContextImpl path = RequestThreadInfo.get().getApplication();
                if (path == null) {
                        super.checkRead(file);
                } else {
                        //boolean b = file.startsWith(path.getAppPath());

                        
                        try {
                                boolean b = false;
                                if (file.indexOf("..") > -1)
                                        b = new java.io.File(file).getCanonicalPath().startsWith(path.getAppPath()); // this is slow
                                else
                                        b = file.startsWith(path.getAppPath());

                                if (b) {
                                        return;
                                } else {
                                        super.checkRead(file);
                                }
                        } catch (IOException e) {
                                throw new SecurityException(e);
                        } catch (SecurityException e) {
                                //System.out.println("file.startsWith - " + file  + ", " + path.getAppPath());
                                throw e;
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
                ServletContextImpl path = RequestThreadInfo.get().getApplication();
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
                ServletContextImpl path = RequestThreadInfo.get().getApplication();
                if (path != null) {
                        if ((pkg.equals("javax.swing") && !allowSwing) || (pkg.equals("java.awt") && allowAWT)) {
                                throw new SecurityException("Access denied for package: " + pkg);
                        }
                }
                super.checkPackageAccess(pkg);
        }
}
