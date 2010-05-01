package com.sybrix.easygsp.http;

import com.sybrix.easygsp.http.Worker;
import com.sybrix.easygsp.server.EasyGServer;

import java.util.List;
import java.util.Collections;
import java.util.Stack;

public class WorkerPool {

        private List<Worker> workers = Collections.synchronizedList(new Stack<Worker>());
        private int min_threads;
        private int max_threads;
        private static WorkerPool _instance;

        public static WorkerPool getInstance() {
                if (_instance == null)
                        _instance = new WorkerPool(EasyGServer.propertiesFile.getInt("threadpool.min",5), EasyGServer.propertiesFile.getInt("threadpool.max",100));

                return _instance;
        }

        public WorkerPool(int min_threads, int max_threads) {
                this.min_threads = min_threads;
                this.max_threads = max_threads;

                for (int x = 0; x < min_threads; x++) {
                        createNewWorker();
                }
        }


        private void createNewWorker() {
                Worker xp = new Worker(this, null);
                addWorker(xp);
                xp.start();
        }

        public void addWorker(Worker m) {
                workers.add(m);
                synchronized (workers) {
                        workers.notify();
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
                return workers.remove(0);
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
