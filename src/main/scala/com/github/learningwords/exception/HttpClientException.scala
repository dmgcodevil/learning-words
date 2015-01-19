package com.github.learningwords.exception

/**
 * Created by dmgcodevil on 1/17/2015.
 */
class HttpClientException(msg: String) extends RuntimeException(msg) {}

object HttpClientException {

  def apply(): HttpClientException = new HttpClientException(null)

  def create(msg: String): HttpClientException = new HttpClientException(msg)

  def create(msg: String, cause: Throwable) = new HttpClientException(msg).initCause(cause)

  def create(cause: Throwable) = new HttpClientException(null).initCause(cause)
}