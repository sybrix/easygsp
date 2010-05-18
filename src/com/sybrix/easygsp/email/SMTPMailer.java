package com.sybrix.easygsp.email;       


import com.sybrix.easygsp.email.Email;
import com.sybrix.easygsp.exception.SMTPMailerException;
import com.sybrix.easygsp.exception.SendEmailException;
import com.sybrix.easygsp.http.StaticControllerMethods;
import easygsp.ByteArrayDataSource;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SMTPMailer {
        private static final Logger log = Logger.getLogger(SMTPMailer.class.getName());

        public static void send(final Email email) throws SMTPMailerException {
                Transport transport = null;
                try {
                        Session session = null;
                        Properties props = null;

                        if (email.getHost().endsWith(".gmail.com")) {
                                props = new Properties();
                                props.setProperty("mail.transport.protocol", "smtp");
                                props.setProperty("mail.host", email.getHost());
                                props.put("mail.smtp.auth", "true");
                                props.put("mail.smtp.port", "465");
                                props.put("mail.smtp.socketFactory.port", "465");
                                props.put("mail.smtp.socketFactory.class",
                                        "javax.net.ssl.SSLSocketFactory");
                                props.put("mail.smtp.socketFactory.fallback", "false");
                                props.setProperty("mail.smtp.quitwait", "false");

                                session = Session.getInstance(props,
                                        new javax.mail.Authenticator() {
                                                protected PasswordAuthentication getPasswordAuthentication() {
                                                        return new PasswordAuthentication(email.getUsername(), email.getPassword());
                                                }
                                        });
                        } else {

                                props = System.getProperties();
                                if (email.isAuthenticationRequired()) {
                                        props.put("mail.smtp.auth", "true");
                                } else {
                                        props.put("mail.smtp.auth", "false");
                                }
                                session = Session.getInstance(props, null);
                        }

                        MimeMessage message = new MimeMessage(session);

                        message.setFrom(new InternetAddress(email.getFrom()));

                        List<String> recipients = email.getRecipients();
                        List<String> bccRecipients = email.getBcc();
                        List<String> ccRecipients = email.getCc();

                        Address[] to_recipients = parseRecipients(recipients);
                        Address[] cc_recipients = parseRecipients(ccRecipients);
                        Address[] bcc_recipients = parseRecipients(bccRecipients);

                        message.addRecipients(Message.RecipientType.TO, to_recipients);
                        message.addRecipients(Message.RecipientType.CC, cc_recipients);
                        message.addRecipients(Message.RecipientType.BCC, bcc_recipients);

                        message.setSubject(email.getSubject());

                        Multipart multipart = new MimeMultipart("alternative");

                        // create plain text
                        if (email.getBody() != null) {
                                BodyPart plainText = new MimeBodyPart();
                                plainText.setText(email.getBody());
                                multipart.addBodyPart(plainText);
                        }

                        // create html content
                        if (email.getHtmlBody() != null) {
                                BodyPart htmlContent = new MimeBodyPart();
                                htmlContent.setText(email.getHtmlBody());
                                htmlContent.setContent(email.getHtmlBody(), "text/html");
                                multipart.addBodyPart(htmlContent);
                        }

                        for (String key : email.getAttachments().keySet()) {
                                // add attachments
                                BodyPart attachment = new MimeBodyPart();
                                Object data = email.getAttachments().get(key);
                                DataSource source = null;
                                if (data instanceof File) {
                                        source = new FileDataSource((File) data);
                                } else if (data instanceof String) {
                                        source = new FileDataSource(data.toString());
                                } else if (data instanceof ByteArrayDataSource) {
                                        source = (ByteArrayDataSource) data;
                                } else {
                                        continue;
                                }

                                attachment.setDataHandler(new DataHandler(source));
                                attachment.setFileName(key);
                                multipart.addBodyPart(attachment);
                        }

                        message.setContent(multipart);
                        message.saveChanges();

                        transport = session.getTransport("smtp");
                        transport.connect(email.getHost(), email.getPort(), email.getUsername(), email.getPassword());
                        transport.send(message, message.getAllRecipients());


                } catch (SendEmailException e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                        StaticControllerMethods.addLogAppThrowableAndMessageMethod(email.getApp(), e.getMessage(), e);
                        throw e;
                } catch (Throwable e) {
                        StaticControllerMethods.addLogAppThrowableAndMessageMethod(email.getApp(), e.getMessage(), e);
                        throw new SendEmailException(e);
                } finally {
                        try {
                                if (transport != null)
                                        transport.close();
                        } catch (Exception e) {
                                // do nothing
                        }
                }
        }

//        public static void sendGmail(final Email email) throws SMTPMailerException {
//                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
//                try {
//                        Properties props = new Properties();
//                        props.setProperty("mail.transport.protocol", "smtp");
//                        props.setProperty("mail.host", email.getHost());
//                        props.put("mail.smtp.auth", "true");
//                        props.put("mail.smtp.port", "465");
//                        props.put("mail.smtp.socketFactory.port", "465");
//                        props.put("mail.smtp.socketFactory.class",
//                                "javax.net.ssl.SSLSocketFactory");
//                        props.put("mail.smtp.socketFactory.fallback", "false");
//                        props.setProperty("mail.smtp.quitwait", "false");
//
//                        Session session = Session.getInstance(props,
//                                new javax.mail.Authenticator() {
//                                        protected PasswordAuthentication getPasswordAuthentication() {
//                                                return new PasswordAuthentication(email.getUsername(), email.getPassword());
//                                        }
//                                });
//
//                        MimeMessage message = new MimeMessage(session);
//
//                        message.setSender(new InternetAddress(email.getFrom()));
//                        message.setSubject(email.getSubject());
//                        if (email.isHtml()) {
//                                message.setContent(email.getBody(), "text/html; charset=\"us-ascii\"");
//                        } else {
//                                message.setText(email.getBody());
//                        }
////                               if (recipients.indexOf(',') > 0)
////                                                       message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
////                               else
////                                                       message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
//
//                        List<String> recipients = email.getRecipients();
//                        List<String> bccRecipients = email.getBcc();
//                        List<String> ccRecipients = email.getCc();
//
//                        Address[] to_recipients = parseRecipients(recipients);
//                        Address[] cc_recipients = parseRecipients(ccRecipients);
//                        Address[] bcc_recipients = parseRecipients(bccRecipients);
//
//
//                        message.addRecipients(Message.RecipientType.TO, to_recipients);
//                        message.addRecipients(Message.RecipientType.CC, cc_recipients);
//                        message.addRecipients(Message.RecipientType.BCC, bcc_recipients);
//                        message.saveChanges();
//
//                        Transport.send(message);
//                } catch (Exception e) {
//
//                        throw new SMTPMailerException(e);
//                }
//
//        }

        private static Address[] parseRecipients(List<String> list) throws AddressException {
                Address rec[] = new InternetAddress[list.size()];
                int x = 0;
                for (Object to : list) {
                        rec[x++] = new InternetAddress((String) to);
                }
                return rec;
        }


}
