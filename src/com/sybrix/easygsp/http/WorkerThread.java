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

import java.net.Socket;

/**
 * WorkerThread <br/>
 *
 * @author David Lee
 */
public class WorkerThread extends Thread {
        private long requestStartTime;
        private long requestEndTime;
        private long stopTime;
        private Socket socket;
        private RequestThread2 requestThread;

        private volatile boolean stopRequested;

        public long getRequestStartTime() {
                return requestStartTime;
        }

        public void setRequestStartTime(long requestStartTime) {
                this.requestStartTime = requestStartTime;
        }

        public long getRequestEndTime() {
                return requestEndTime;
        }

        public void setRequestEndTime(long requestEndTime) {
                this.requestEndTime = requestEndTime;
        }

        public long getStopTime() {
                return stopTime;
        }

        public void setStopTime(long stopTime) {
                this.stopTime = stopTime;
        }

        public Socket getSocket() {
                return socket;
        }

        public void setSocket(Socket socket) {
                this.socket = socket;
        }

        public RequestThread2 getRequestThread() {
                return requestThread;
        }

        public void setRequestThread(RequestThread2 requestThread) {
                this.requestThread = requestThread;
        }

        @Override
        public void run() {
                while (!stopRequested) {
                        try {
                                synchronized (this) {
                                        wait();
                                }
                        } catch (InterruptedException e) {

                        }

                        doWork();

                }
        }

        private void doWork() {
                //requestThread.processRequest();
        }

        public void process() {
                synchronized (this){
                        interrupt();
                }
        }
}
