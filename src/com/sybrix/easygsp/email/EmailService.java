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

package com.sybrix.easygsp.email;

import com.sybrix.easygsp.exception.SMTPMailerException;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * LoggerThread <br/>
 *                                                                
 * @author David Lee
 */
public class EmailService {
        private static final Logger logger = Logger.getLogger(EmailThread.class.getName());

        private static EmailThread emailThread;

        public static void start() {
                emailThread = new EmailThread();
                emailThread.start();
        }

        public static void addEmail(Email email) {
                emailThread.addEmail(email);
        }

        public static void stop() {
                emailThread.stopThread();
        }

        private static class EmailThread extends Thread {

                private volatile boolean stopped = false;
                private List<Email> emails = Collections.synchronizedList(new ArrayList());

                @Override
                public void run() {
                        logger.info("EmailService thread started");
                        while (true) {
                                if (emails.size() == 0){
                                        synchronized (emails) {
                                                try {
                                                        emails.wait();
                                                } catch (InterruptedException e) {

                                                }
                                        }
                                }
                                
                                if (stopped && emails.size() == 0)
                                        break;

                                sendEmail(emails.remove(0));
                        }

                        logger.info("EmailService thread stopped");
                }

                public void sendEmail(Email email) {
                        try {
                                logger.fine("sending email: " + email);
                                SMTPMailer.send(email);
                        } catch (SMTPMailerException e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                }

                public void addEmail(Email email) {
                        synchronized (emails) {
                                emails.add(email);
                                emails.notifyAll();
                        }
                }

                public int emailsInQueue() {
                        return emails.size();
                }

                public void stopThread() {
                        this.stopped = true;
                        logger.fine("EmailService thread stop requested...");
                        this.interrupt();
                }
        }

}