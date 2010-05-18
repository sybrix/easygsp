package com.sybrix.easygsp.http;

import java.lang.InterruptedException;
import java.util.Map;
import java.util.logging.Logger;
import java.net.Socket;

public class Worker extends Thread {
        private static final Logger log = Logger.getLogger(Worker.class.getName());
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
                log.fine("worker " + id + " ready");

                while (!stopRequested) {
                        try {

                                try {
                                        this.wait();
                                } catch (InterruptedException e) {
                                        if (stopRequested)
                                                break;
                                }

                                log.finer("worker " + id + " awake");

                                processClientRequest();

                                // return worker to pool
                                if (workerPool.getWorkerCount() < workerPool.getMaxWorkerCount()) {
                                        log.finest("adding working back to pool");

                                        workerPool.addWorker(this);
                                        this.notify();
                                        log.finest(workerPool.getWorkerCount() + " workers in pool");
                                }

                        } catch (Exception e) {
                                      e.printStackTrace();
                        }
                }

                log.finer("worker " + id + " thread stopped");
                log.finer(workerPool.getWorkerCount() + " workers in pool");
        }

        private void processClientRequest() {
             //   requestThread.runIt();
        }


        public synchronized void killThread() {
                stopRequested = true;
                this.notify();
        }
}
