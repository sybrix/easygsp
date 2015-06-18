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

import java.lang.InterruptedException;
import java.util.logging.Logger;

public class Worker extends Thread {
        private static final Logger logger = Logger.getLogger(Worker.class.getName());
        private static long ctr = 0;
        private long id = ++ctr;

        private volatile boolean stopRequested = false;

        private WorkerPool workerPool;
        private RequestThread requestThread;

        public Worker(WorkerPool mgr) {
                super("Worker Thread - " + ctr);
                this.workerPool = mgr;
        }

        public synchronized void processRequest(RequestThread requestThread) {
                this.requestThread = requestThread;
                this.notifyAll();
        }


        public synchronized void run() {
                logger.fine("worker " + id + " ready");

                while (!stopRequested) {
                        try {

                                try {
                                        this.wait();
                                } catch (InterruptedException e) {
                                        if (stopRequested)
                                                break;
                                }

                                logger.finer("worker " + id + " awake");

                                processClientRequest();

                                // return worker to pool
                                if (workerPool.getWorkerCount() < workerPool.getMaxWorkerCount()) {
                                        logger.finest("adding working back to pool");

                                        workerPool.addWorker(this);
                                        this.notify();
                                        logger.finest(workerPool.getWorkerCount() + " workers in pool");
                                }

                        } catch (Exception e) {
                                      e.printStackTrace();
                        }
                }

                logger.finer("worker " + id + " thread stopped");
                logger.finer(workerPool.getWorkerCount() + " workers in pool");
        }

        private void processClientRequest() {
             //   requestThread.runIt();
        }


        public synchronized void killThread() {
                stopRequested = true;
                this.notify();
        }
}
