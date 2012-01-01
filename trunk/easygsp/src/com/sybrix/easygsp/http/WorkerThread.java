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
