package com.sybrix.easygsp.http;

import com.sybrix.easygsp.server.EasyGServer;

import java.util.logging.Logger;

/**
 * LoggerThread <br/>
 *
 * @author David Lee
 */
public class LoggerThread extends Thread{
        private static final Logger log = Logger.getLogger(LoggerThread.class.getName());
        private static int checkInterval;
        private volatile boolean stopped = false;
        static {
                checkInterval = EasyGServer.propertiesFile.getInt("logger.checkInterval.seconds",5) * 1000;
        }

        @Override
        public void run() {
                log.fine("Logger thread started");
                while(!stopped){
                        try {
                               Thread.sleep(checkInterval);
                        } catch(InterruptedException e){
                                // do nothing
                        }

                        EasyGSPLogger.getInstance().logMessagesInQueue();
                }

                log.fine("Logger thread stopped");
        }

        public boolean messagesInQueue(){
                return EasyGSPLogger.getInstance().getQueueSize() == 0;
        }

        public void stopLogging() {
                this.stopped = true;
                log.fine("Logger thread stop requested");
                this.interrupt();
        }
}
