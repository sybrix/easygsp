package com.sybrix.easygsp.logging;

import com.sybrix.easygsp.http.ServletContextImpl;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * LogMessage <br/>
 *
 * @author David Lee
 */
public class LogMessage {
                                        
        private String message;
        private Throwable exception;
        private Date timestamp;
        private ServletContextImpl application;
        private String formattedMessage;
        private SimpleDateFormat currentTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a z");

        public LogMessage(Throwable exception, ServletContextImpl application) {
                this.exception = exception;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
        }

        public LogMessage(String message, ServletContextImpl application) {
                this.message = message;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
        }

        public LogMessage(String message, Throwable exception, ServletContextImpl application) {
                this.message = message;
                this.exception = exception;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
        }

        public ServletContextImpl getApplication() {
                return application;
        }

        public void setApplication(ServletContextImpl application) {
                this.application = application;
        }

        public Throwable getException() {
                return exception;
        }

        public void setException(Throwable exception) {
                this.exception = exception;
        }

        public String getMessage() {
                return message;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        public Date getTimestamp() {
                return timestamp;
        }

        public void setTimestamp(Date timestamp) {
                this.timestamp = timestamp;
        }

        public String getFormattedMessage() {
                return formattedMessage;
        }

        public void setFormattedMessage(String formattedMessage) {
                this.formattedMessage = formattedMessage;
        }

        public String toString() {
                if (formattedMessage == null) {
                        formattedMessage = formatMessage();
                }

                return formattedMessage;
        }

        protected String formatMessage() {
                StringBuffer s = new StringBuffer();
                s.append("[").append(currentTimeFormat.format(getTimestamp())).append("] - ");

                if (getMessage() != null) {
                        if (getMessage().trim().length() > 0) {
                                s.append(getMessage());
                                s.append("\r\n");
                        }
                }

                if (getException() != null) {
                        s.append(getException().getMessage()).append("\r\n");
                        for (StackTraceElement stackTraceElement : getException().getStackTrace()) {
                                s.append("\tat ").append(stackTraceElement.getClassName())
                                        .append("(").append(stackTraceElement.getMethodName())
                                        .append(":").append(stackTraceElement.getLineNumber()).append(")\r\n");
                        }
                }

                return s.toString();
        }
}
