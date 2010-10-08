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

package com.sybrix.easygsp.http;

import com.sybrix.easygsp.http.RequestThread;
import com.sybrix.easygsp.server.EasyGServer;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * ThreadMonitor <br/><br/>
 * <p/>
 * Description :<br/>
 * Does exactly what is says.  It monitors running request threads and kills them after the thread.timeout value has elapsed.
 * Thead.stop() is used to stop running threads.
 */
public class ThreadMonitor {
        private static final Logger log = Logger.getLogger(ThreadMonitor.class.getName());
        private volatile static Monitor monitor;

        private ThreadMonitor() {
        }

        public static void start() {
                monitor = new Monitor();
                monitor.setDaemon(true);
                monitor.start();

                log.info("Thread monitor started  (timeout: " + EasyGServer.propertiesFile.getInt("thread.timeout") + ")");
        }

        public static void add(RequestThread thread) {
                log.fine("adding thread id:" + thread.getId() + " from running map");
                monitor.add(thread);
        }
//
//        public static void remove(RequestThread thread) {
//                log.finest("removing thread id:" + thread.getId() + " from running map");
//                monitor.stopList.remove(thread);
//        }

        public static boolean isEmpty() {
                return monitor.stopList.isEmpty() && monitor.pendingList.isEmpty();
        }

        public static int size() {
                log.info("pending: " + monitor.pendingList.size());
                log.info("stopList: " + monitor.stopList.size());
                return monitor.stopList.size() + monitor.pendingList.size();

        }

        public static void stopMonitoring() {
                synchronized (monitor) {
                        monitor.monitoring = false;
                        monitor.interrupt();
                        log.fine("ThreadMonitor stop requested...");
                }
        }

        static class Monitor extends Thread {

                public volatile boolean monitoring = true;
                private volatile List<RequestThread> pendingList;


                /**
                 * contains threads to be stopped, on accessed by "this" thread, no need to synchronize on it
                 */
                private volatile List<RequestThread> stopList;

                public Monitor() {
                        this.stopList = new ArrayList();
                        this.pendingList = new ArrayList();
                }

                public void run() {
                        while (monitoring) {
                                try {
                                        // synchronize on pendlist list for everything
                                        synchronized (pendingList) {
                                                if (pendingList.isEmpty() && !stopList.isEmpty()) {
                                                        pendingList.wait(2000);// something more precise might be possible here
                                                } else if (pendingList.isEmpty() && stopList.isEmpty()) {
                                                        pendingList.wait();
                                                } else {
                                                        stopList.addAll(pendingList);
                                                        pendingList.clear();
                                                }
                                        }

                                } catch (InterruptedException e) {
                                        log.fine("monitoring working....");
                                }


                                // the killing of a thread does not need to happen at the precise moment the timeout value has passed.
                                // After the time has passed the thread should be killed, the rest can remain a little fuzzy
                                long currentTime = System.currentTimeMillis();
                                Iterator iter = stopList.iterator();
                                while (iter.hasNext()) {
                                        RequestThread requestThread = (RequestThread) iter.next();

                                        if (currentTime > requestThread.getStopTime()) {


                                                if (requestThread.isAlive()) {
                                                        log.fine("trying to stop a thread ....");


                                                        requestThread.closeSocket();
                                                        try {
                                                                requestThread.interrupt();
                                                        } catch (Exception e) {

                                                        }

                                                        try {
                                                                //requestThread.sendError(500, requestThread.getScriptPath(),requestThread.getApplication().getGroovyScriptEngine(),requestThread.getBinding(), new Exception("Thread exceeded maximum timeout."));
//                                                                requestThread.sendError("Thread exceeded maximum timeout.");
//                                                                requestThread.sleep(500);

                                                                requestThread.stop();
                                                                log.fine("stopped called on thread ....");
                                                        } catch (Throwable e) {
                                                                // do nothing there.  An exception will be throw in the run of RequestThread
                                                                // catch it and log it there
                                                                e.getStackTrace();
                                                        }
                                                }

                                                iter.remove();
                                        } else if (requestThread.getRequestEndTime() > 0) {
                                                iter.remove();
                                        }
                                }
                        }

                        log.fine("ThreadMonitor stopped");
                }

                public void add(RequestThread t) {
                        synchronized (pendingList) {
                                pendingList.add(t);
                                pendingList.notify();
                        }
                }

        }
}
