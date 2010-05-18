package com.sybrix.easygsp.exception;

public class SMTPMailerException extends Exception {
	public SMTPMailerException(String msg) {
		super(msg);
	}

        public SMTPMailerException(Exception msg) {
		super(msg);
	}

}
