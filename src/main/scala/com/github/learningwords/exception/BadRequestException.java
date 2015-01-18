package com.github.learningwords.exception;

/**
 * Created by dmgcodevil on 1/17/2015.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException() {
    }

    public BadRequestException(String detailMessage) {
        super(detailMessage);
    }

    public BadRequestException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BadRequestException(Throwable throwable) {
        super(throwable);
    }
}