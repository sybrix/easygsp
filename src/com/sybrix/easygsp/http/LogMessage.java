package com.sybrix.easygsp.http;

import java.util.Date;

/**
 * LogMessage <br/>
 *
 * @author David Lee
 */
public class LogMessage   {

        private String message;
        private Throwable exception;
        private Date timestamp;
        private Application application;

        public LogMessage(Throwable exception, Application application) {
                this.exception = exception;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
        }

        public LogMessage(String message, Application application) {
                this.message = message;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
        }

        public LogMessage(String message, Throwable exception, Application application) {
                this.message = message;
                this.exception = exception;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
        }

        public Application getApplication() {
                return application;
        }

        public void setApplication(Application application) {
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
}
