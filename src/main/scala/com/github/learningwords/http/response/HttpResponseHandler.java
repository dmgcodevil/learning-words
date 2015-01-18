package com.github.learningwords.http.response;

import org.apache.http.HttpResponse;

/**
 * Used to handle response after a http request.
 * Created by dmgcodevil on 2.9.14.
 */
public interface HttpResponseHandler {

    void handle(HttpResponse response);
}
