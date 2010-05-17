package com.sybrix.easygsp.http;

import com.sybrix.easygsp.http.Worker;
import com.sybrix.easygsp.server.EasyGServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class WorkerPool {

        private List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());
        private int min_threads;
        private int max_threads;
        private static WorkerPool _instance;

        public static WorkerPool getInstance() {
                if (_instance == null)
                        _instance = new WorkerPool(EasyGServer.propertiesFile.getInt("threadpool.min",5), EasyGServer.propertiesFile.getInt("threadpool.max",10));

                return _instance;
        }

        public WorkerPool(int min_threads, int max_threads) {
                this.min_threads = min_threads;
                this.max_threads = max_threads;

                for (int x = 0; x < min_threads; x++) {
                        addNewWorkerToQueue();
                }
        }


        private void addNewWorkerToQueue() {
                Worker xp = new Worker(this);
                addWorker(xp);
                xp.start();
        }

        private Worker createNewWorkerToQueue() {
                Worker xp = new Worker(this);
                xp.start();

                return xp;
        }

        public void addWorker(Worker m) {
                workers.add(m);
                synchronized (workers) {
                        workers.notifyAll();
                }
        }

        public synchronized void killThreads() {
                synchronized (workers) {
                        for (Worker w : workers) {
                                w.killThread();
                        }
                }
        }

        public Worker getWorker() {
                Worker w  = workers.remove(0);
                if (w == null)
                        w = createNewWorkerToQueue();

                return w;
        }

        public int getMinWorkerCount() {
                return min_threads;
        }

        public int getMaxWorkerCount() {
                return max_threads;
        }

        public int getWorkerCount() {
                return workers.size();
        }
}
