package com.sybrix.easygsp.http;

import java.lang.InterruptedException;
import java.util.Map;
import java.util.logging.Logger;
import java.net.Socket;

public class Worker extends Thread {
        private static final Logger log = Logger.getLogger(Worker.class.getName());
        private static long ctr = 0;
        private volatile boolean stopRequested = false;

        private WorkerPool workerPool;
        private Map applications;
        private Socket socket;

        public Worker(WorkerPool mgr, Map applications) {
                super("Worker Thread-" + ctr);
                this.workerPool = mgr;
                this.applications = applications;
        }

        public void processRequest() {
                this.notify();
        }

        public Socket getSocket() {
                return socket;
        }

        public void setSocket(Socket socket) {
                this.socket = socket;
        }

        public synchronized void run() {
                log.fine("worker " + ctr + " ready");

                while (!stopRequested) {
                        try {

                                try {
                                        this.wait();
                                } catch (InterruptedException e) {
                                        if (stopRequested)
                                                break;
                                }

                                log.finer("worker " + ctr + " awake");

                                processClientRequest();

                                // return worker to pool
                                if (workerPool.getWorkerCount() < workerPool.getMaxWorkerCount()) {
                                        log.finest("adding working back to pool");
                                        socket = null;
                                        workerPool.addWorker(this);
                                        log.finest(workerPool.getWorkerCount() + " workers in pool");
                                }

                        } catch (Exception e) {

                        }
                }

                log.finer("worker " + ctr + " thread stopped");
                log.finer(workerPool.getWorkerCount() + " workers in pool");
        }

        private void processClientRequest() {
        }


        public synchronized void killThread() {
                stopRequested = true;
                this.notify();
        }
}
