package com.sybrix.easygsp.exception;

/**
 * SendEmailException <br/>
 *
 * @author David Lee
 */
public class SendEmailException extends RuntimeException {
        public SendEmailException() {
        }

        public SendEmailException(String message) {
                super(message);
        }

        public SendEmailException(String message, Throwable cause) {
                super(message, cause);
        }

        public SendEmailException(Throwable cause) {
                super(cause);
        }
}
