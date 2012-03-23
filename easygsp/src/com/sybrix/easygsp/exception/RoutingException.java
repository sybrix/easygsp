package com.sybrix.easygsp.exception;

import org.apache.regexp.RE;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 3/19/12
 * Time: 1:47 AM
 */
public class RoutingException extends RuntimeException {
        public RoutingException(String message){
                super(message);
        }

        public RoutingException(String message, Throwable cause) {
                super(message, cause);
        }
}
