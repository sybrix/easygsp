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

package com.sybrix.easygsp.logging;

import com.sybrix.easygsp.server.EasyGServer;

import java.util.logging.Logger;

/**
 * LoggerThread <br/>
 *
 * @author David Lee
 */
public class LoggerThread extends Thread{
        private static final Logger logger = Logger.getLogger(LoggerThread.class.getName());
        private static int checkInterval;
        private volatile boolean stopped = false;
        
        static {
                checkInterval = EasyGServer.propertiesFile.getInt("logger.checkInterval.seconds",5) * 1000;
        }

        @Override
        public void run() {
                logger.info("Logger thread started");
                while(!stopped){
                        try {
                               Thread.sleep(checkInterval);
                        } catch(InterruptedException e){
                                // do nothing
                        }

                        EasyGSPLogger.getInstance().logMessagesInQueue();
                }

                logger.info("Logger thread stopped");
        }

        public boolean messagesInQueue(){
                return EasyGSPLogger.getInstance().getQueueSize() > 0;
        }

        public void stopLogging() {
                this.stopped = true;
                logger.fine("Logger thread stop requested...");
                this.interrupt();
        }
}
