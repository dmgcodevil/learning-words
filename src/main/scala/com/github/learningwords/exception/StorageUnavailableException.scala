package com.github.learningwords.exception


class StorageUnavailableException(msg: String) extends RuntimeException(msg) {}

object StorageUnavailableException {

  def apply(): StorageUnavailableException = new StorageUnavailableException(null)

  def create(msg: String): StorageUnavailableException = new StorageUnavailableException(msg)

  def create(msg: String, cause: Throwable) = new StorageUnavailableException(msg).initCause(cause)

  def create(cause: Throwable) = new StorageUnavailableException(null).initCause(cause)
}