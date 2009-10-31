package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;

import java.util.Map;
import java.util.logging.Logger;

public class SessionMonitor extends Thread {
        private static Logger log = Logger.getLogger(SessionMonitor.class.getName());

        private Map<String, Application> applications;
        private volatile boolean stopped = false;

        public SessionMonitor(Map applications) {
                this.applications = applications;
        }

        public void run() {
                log.info("SessionMonitor started.") ;
                long timeOut = EasyGServer.propertiesFile.getInt("session.timeout") * 1000 * 60;

                while (!stopped) {
                        try {
                                Thread.sleep(2000);
                        } catch (InterruptedException e) {

                        }
        
                        for (Application app : applications.values()) {
                                for (SessionImpl session : app.getSessions().values()) {
                                        long diff = System.currentTimeMillis() - session.getLastAccessedTime();
                                        if (diff >= timeOut) {
                                                log.fine("stopping session: " + session.getId() + ", app: " + app.getAppName());

                                                try {
                                                        // threaded ?
                                                        if (app.groovyWebFileExists())
                                                                app.invokeWebMethod("onSessionEnd", session);

                                                } catch (Exception e) {
                                                       log.fine("onSessionStart failed for session:" + session.getId() + ", app:" + app.getAppName());
                                                }
                                               session.invalidate();
                                        }
                                }
                        }
                }

                log.info("Session monitor stopped");
        }

        public void stopThread() {
                stopped = true;
                this.interrupt();
        }
}
