package com.sybrix.easygsp.exception;

/**
 * WebDotGroovyFileFailed <br/>
 *
 * @author David Lee
 */
public class WebDotGroovyFileException extends RuntimeException{
        public WebDotGroovyFileException() {
        }
        public WebDotGroovyFileException(String message) {
                super(message);
        }
        public WebDotGroovyFileException(String message, Throwable cause) {
                super(message, cause);
        }
        public WebDotGroovyFileException(Throwable cause) {
                super(cause);
        }
}
