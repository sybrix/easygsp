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

import java.util.*;
import java.util.logging.Level;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Logger <br/>
 * Description :
 */
public class EasyGSPLogger {
        private static EasyGSPLogger _instance;

        public static EasyGSPLogger getInstance() {
                if (_instance == null) {
                        _instance = new EasyGSPLogger();
                }

                return _instance;
        }

        private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EasyGSPLogger.class.getName());

        private volatile List<LogMessage> incomingMessages = Collections.synchronizedList(new ArrayList<LogMessage>());
        private volatile List<LogMessage> messageQueue = new ArrayList<LogMessage>();
        private SimpleDateFormat fileNameSDF = new SimpleDateFormat("MM_dd_yyyy");
         

        public void logMessage(LogMessage message) {
                incomingMessages.add(message);
        }

        protected synchronized void logMessagesInQueue() {
                int numberOfMessages = incomingMessages.size();
                if (numberOfMessages > 50) {
                        numberOfMessages = 50;
                }

                if (numberOfMessages ==0)
                        return;

                logger.fine("logging " + numberOfMessages + " message(s) in log queue");
                synchronized (incomingMessages) {
                        for (int i = 0; i < numberOfMessages; i++) {
                                messageQueue.add(incomingMessages.remove(0));
                        }

                         incomingMessages.notifyAll();
                }

               
                for (LogMessage message : messageQueue) {
                        writeToFile(message);
                }

                messageQueue.clear();
        }

        public int getQueueSize(){
                return incomingMessages.size() + messageQueue.size();
        }


        private void writeToFile(LogMessage message) {
                File logFile = null;
                FileWriter fileWriter = null;

                if (message.getApplication().getLoggingLevel().ordinal() > message.getLevel().ordinal())
                        return;

                try {
                        logFile = new File(message.getApplication().getAppPath() + File.separator + "WEB-INF" + File.separator + "logs" + File.separator + "app_" + fileNameSDF.format(new Date()) + ".log");
                        logFile.getParentFile().mkdirs();

                        fileWriter = new FileWriter(logFile, true);

                        fileWriter.write(message.toString());

                } catch (IOException e) {
                        logger.log(Level.SEVERE, "Logger.writeToFile() failed. message:" + e.getMessage() + ", application: " + message.getApplication().getAppPath(), e);
                } finally {
                        try {
                                fileWriter.close();
                        } catch (IOException e) {

                        }
                }
        }

}
