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