package com.sybrix.easygsp.http;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
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

        private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(EasyGSPLogger.class.getName());

        private List<LogMessage> incomingMessages = Collections.synchronizedList(new ArrayList<LogMessage>());
        private List<LogMessage> messageQueue = new ArrayList<LogMessage>();
        private SimpleDateFormat fileNameSDF = new SimpleDateFormat("MM_dd_yyyy");
        

        public void log(LogMessage message) {
                incomingMessages.add(message);
        }

        protected synchronized void logMessagesInQueue() {
                int numberOfMessages = incomingMessages.size();
                if (numberOfMessages > 50) {
                        numberOfMessages = 50;
                }

                if (numberOfMessages ==0)
                        return;

                log.fine("logging " + numberOfMessages + " message(s) in log queue");
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

                try {
                        logFile = new File(message.getApplication().getAppPath() + File.separator + "WEB-INF" + File.separator + "logs" + File.separator + "app_" + fileNameSDF.format(new Date()) + ".log");
                        logFile.getParentFile().mkdirs();

                        fileWriter = new FileWriter(logFile, true);

                        fileWriter.write(message.toString());

                } catch (IOException e) {
                        log.log(Level.SEVERE, "Logger.writeToFile() failed. message:" + e.getMessage() + ", application: " + message.getApplication().getAppPath(), e);
                } finally {
                        try {
                                fileWriter.close();
                        } catch (IOException e) {

                        }
                }
        }

}
