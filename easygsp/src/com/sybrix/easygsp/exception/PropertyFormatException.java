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
                super(message);    //To change body of overridden methods use File | Settings | File Templates.
        }

        public PropertyFormatException(String message, Throwable cause) {
                super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
        }

        public PropertyFormatException(Throwable cause) {
                super(cause);    //To change body of overridden methods use File | Settings | File Templates.
        }
}
