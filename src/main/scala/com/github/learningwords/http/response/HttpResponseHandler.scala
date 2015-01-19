package com.github.learningwords.http.response

import org.apache.http.HttpResponse

/**
 * Used to handle response after a http request.
 * Created by dmgcodevil on 2.9.14.
 */
trait HttpResponseHandler {

  def handle(response: HttpResponse)
}
