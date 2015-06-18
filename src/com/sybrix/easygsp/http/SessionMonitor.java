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
import java.util.logging.Logger;

public class SessionMonitor extends Thread {
        private static Logger logger = Logger.getLogger(SessionMonitor.class.getName());

        private Map<String, ServletContextImpl> applications;
        private volatile boolean stopped = false;

        public SessionMonitor(Map applications) {
                this.applications = applications;
        }

        public void run() {
                logger.info("SessionMonitor started.") ;
                long timeOut = EasyGServer.propertiesFile.getInt("session.timeout", 15) * 1000 * 60;

                while (!stopped) {
                        try {
                                Thread.sleep(2000);
                        } catch (InterruptedException e) {

                        }
        
                        for (ServletContextImpl app : applications.values()) {
                                for (SessionImpl session : app.getSessions().values()) {
                                        long diff = System.currentTimeMillis() - session.getLastAccessedTime();
                                        if (diff >= timeOut) {
                                                logger.fine("stopping session: " + session.getId() + ", app: " + app.getAppName());
                                                RequestThreadInfo.get().setApplication(app);
                                                try {
                                                        // threaded ?
                                                        if (app.groovyWebFileExists())
                                                                app.invokeWebMethod("onSessionEnd", new Object[]{session});

                                                } catch (Exception e) {
                                                       logger.fine("onSessionStart failed for session:" + session.getId() + ", app:" + app.getAppName());
                                                }
                                               session.invalidate();
                                        }
                                }
                        }
                }

                logger.info("Session monitor stopped");
        }

        public void stopThread() {
                stopped = true;
                this.interrupt();
        }
}
