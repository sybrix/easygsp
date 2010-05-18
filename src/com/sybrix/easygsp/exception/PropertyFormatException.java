package com.sybrix.easygsp.exception;

/**
 * PropertyFormatException <br/>
 *
 * @author David Lee
 */
public class PropertyFormatException extends RuntimeException{
        public PropertyFormatException() {
                super();    
        }

        public PropertyFormatException(String message) {
                super(message);
        }

        public PropertyFormatException(String message, Throwable cause) {
                super(message, cause);
        }

        public PropertyFormatException(Throwable cause) {
                super(cause);
        }
}
