package com.sybrix.easygsp.http;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * RequestError <br/>
 *
 * @author David Lee
 */
public class RequestError {
        private Throwable exception;
        private int lineNumber;
        private String errorMessage;
        private String stackTraceString;
        private String source;
        private String scriptPath;
        //private String templatePath;
        private String exceptionName;
        private String lineNumberMessage;
        private int errorCode;

        public Throwable getException() {
                return exception;
        }

        public void setException(Throwable exception, String appPath, String appPath2) {
                this.exception = exception;
                if (exception == null)
                        return;

                exceptionName = exception.getClass().getName();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);

                if (RequestThreadInfo.get().getUniqueTemplateScriptName() != null)
                        stackTraceString = sw.toString().replaceAll("\n", "<br/>").replace(appPath, "").replace(appPath2, "")
                                .replaceAll(RequestThreadInfo.get().getUniqueTemplateScriptName(), RequestThreadInfo.get().getParsedRequest().getRequestURI());
                else
                        stackTraceString = sw.toString().replaceAll("\n", "<br/>").replace(appPath, "").replace(appPath2, "");
        }

        public int getLineNumber() {
                return lineNumber;
        }
        public void setLineNumber(int lineNumber) {
                this.lineNumber = lineNumber;
        }
        public String getErrorMessage() {
                return errorMessage;
        }
        public void setErrorMessage(String errorMessage) {
                if (RequestThreadInfo.get().getUniqueTemplateScriptName() != null)
                        this.errorMessage = errorMessage.replaceAll(RequestThreadInfo.get().getUniqueTemplateScriptName(), RequestThreadInfo.get().getParsedRequest().getRequestURI());
                else
                        this.errorMessage = errorMessage;
        }
        public String getStackTraceString() {
                return stackTraceString;
        }
        public void setStackTraceString(String stackTraceString) {
                this.stackTraceString = stackTraceString;
        }
        public String getSource() {
                return source;
        }
        public void setSource(String source) {
                this.source = source;
        }

        public String getScriptPath() {
                return scriptPath;
        }
        public void setScriptPath(String scriptPath) {
                this.scriptPath = scriptPath;
        }
//        public String getTemplatePath() {
//                return templatePath;
//        }
//        public void setTemplatePath(String templatePath) {
//                this.templatePath = templatePath;
        //        }
        public void setExceptionName(String exceptionName) {
                this.exceptionName = exceptionName;
        }

        public String getExceptionName() {
                return exceptionName;
        }
        public void setLineNumberMessage(String lineNumberMessage) {
                this.lineNumberMessage = lineNumberMessage;
        }
        public String getLineNumberMessage() {
                return lineNumberMessage;
        }

        public int getErrorCode() {
                return errorCode;
        }
        public void setErrorCode(int errorCode) {
                this.errorCode = errorCode;
        }
}
