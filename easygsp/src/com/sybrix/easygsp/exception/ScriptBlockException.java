package com.sybrix.easygsp.exception;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 4/21/12
 * Time: 1:02 PM
 */
public class ScriptBlockException extends Exception {

        public ScriptBlockException(String message) {
                super(message);
        }

        public ScriptBlockException(String message, Throwable cause) {
                super(message, cause);
        }

        public ScriptBlockException(Throwable cause) {
                super(cause);
        }
}
