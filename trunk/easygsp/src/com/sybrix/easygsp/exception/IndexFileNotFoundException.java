package com.sybrix.easygsp.exception;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 4/3/12
 * Time: 11:11 PM
 */
public class IndexFileNotFoundException extends Exception{

        public IndexFileNotFoundException() {
        }

        public IndexFileNotFoundException(String message) {
                super(message);
        }

        public IndexFileNotFoundException(String message, Throwable cause) {
                super(message, cause);
        }

        public IndexFileNotFoundException(Throwable cause) {
                super(cause);
        }
}
