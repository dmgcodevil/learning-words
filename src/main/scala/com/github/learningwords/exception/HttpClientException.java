package com.github.learningwords.exception;

/**
 * Created by dmgcodevil on 1/17/2015.
 */
public class HttpClientException extends RuntimeException {

    public HttpClientException() {
    }

    public HttpClientException(String detailMessage) {
        super(detailMessage);
    }

    public HttpClientException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public HttpClientException(Throwable throwable) {
        super(throwable);
    }
}
