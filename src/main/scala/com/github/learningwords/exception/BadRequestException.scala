package com.github.learningwords.exception

/**
 * Created by dmgcodevil on 1/17/2015.
 */
class BadRequestException(msg: String) extends RuntimeException(msg) {}

object BadRequestException {

  def apply(): BadRequestException = new BadRequestException(null)

  def create(msg: String): BadRequestException = new BadRequestException(msg)

  def create(msg: String, cause: Throwable) = new BadRequestException(msg).initCause(cause)
}