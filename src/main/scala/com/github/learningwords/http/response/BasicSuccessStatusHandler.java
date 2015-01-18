package com.github.learningwords.http.response;

import com.github.learningwords.exception.HttpClientException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

/**
 * Created by dmgcodevil on 1/17/2015.
 */
public class BasicSuccessStatusHandler implements HttpResponseHandler {

    @Override
    public void handle(HttpResponse response) throws HttpClientException {
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new HttpClientException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        }
    }
}
