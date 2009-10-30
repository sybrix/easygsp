/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sybrix.easygsp.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;

/**
 * CustomFormatter <br/>
 * Description :
 */
public class CustomLogFormatter extends Formatter {

        private final static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm.ss");
        private final static String NEW_LINE = System.getProperty("line.separator");
        public CustomLogFormatter() {
                super();
        }

        public String format(LogRecord record) {

                // Create a StringBuffer to contain the formatted record
                // start with the date.
                StringBuffer sb = new StringBuffer();

                // Get the date from the LogRecord and add it to the buffer
                Date date = new Date(record.getMillis());
                sb.append(sdf.format(date));

                sb.append(" [");

                // Get the level name and add it to the buffer
                sb.append(record.getLevel().getName());
                sb.append("]");

                sb.append(" ").append(record.getSourceClassName()).append(".").append(record.getSourceMethodName());

                sb.append("() - ");


                // Get the formatted message (includes localization
                // and substitution of paramters) and add it to the buffer
                sb.append(formatMessage(record)).append(NEW_LINE);
                if (record.getThrown() != null) {
                        sb.append(record.getThrown().toString()).append(NEW_LINE);
                        StackTraceElement[] stackTrace = record.getThrown().getStackTrace();
                        for (StackTraceElement stackTraceElement : stackTrace) {
                                sb.append("\tat ").append(stackTraceElement.getClassName())
                                        .append("(").append(stackTraceElement.getMethodName())
                                        .append(":").append(stackTraceElement.getLineNumber()).append(")").append(NEW_LINE);

                        }
                }
                return sb.toString();
        }
}
