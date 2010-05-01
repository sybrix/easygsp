package com.sybrix.easygsp.http;

import java.io.Serializable;

/**
 * FlashMessage <br/>
 *
 * @author David Lee
 */
public class FlashMessage implements Serializable {
        private boolean expired;
        private String message;

        public FlashMessage(String message) {
                this.message = message;
        }

        public boolean isExpired() {
                return expired;
        }

        public void setExpired(boolean expired) {
                this.expired = expired;
        }

        public String getMessage() {
                return message;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        @Override
        public String toString() {
                return message;
        }
}
