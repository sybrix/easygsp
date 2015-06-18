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

import com.sybrix.easygsp.http.ServletContextImpl;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        private LoggingLevel level;

        public LogMessage(Throwable exception, ServletContextImpl application) {
                this.exception = exception;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
                level = application.getLoggingLevel();
        }

        public LogMessage(String message, ServletContextImpl application) {
                this.message = message;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
                level = application.getLoggingLevel();
        }

        public LogMessage(String message, Throwable exception, ServletContextImpl application) {
                this.message = message;
                this.exception = exception;
                this.timestamp = new Date(System.currentTimeMillis());
                this.application = application;
                level = application.getLoggingLevel();
        }


        public LogMessage(LoggingLevel level, Throwable exception, ServletContextImpl application) {
                this(exception, application);
                this.level = level;
        }

        public LogMessage(LoggingLevel level, String message, ServletContextImpl application) {
                this(message, application);
                this.level = level;
        }

        public LogMessage(LoggingLevel level, String message, Throwable exception, ServletContextImpl application) {
                this(message, exception, application);
                this.level = level;
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

        public LoggingLevel getLevel() {
                return level;
        }

        public void setLevel(LoggingLevel level) {
                this.level = level;
        }

        protected String formatMessage() {
                StringBuffer s = new StringBuffer();
                s.append("[").append(currentTimeFormat.format(getTimestamp())).append("] " + level + " - ");

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
