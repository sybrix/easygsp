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
