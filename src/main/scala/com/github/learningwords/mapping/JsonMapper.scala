package com.github.learningwords.mapping
import java.io.InputStream

trait JsonMapper {

  def readObject[T](json: String, objType: Class[_ <: T]): T

  def readObject[T](is: InputStream, objType: Class[_ <: T]): T

 // def readObject[T, C <: util.Collection[T]](is: InputStream, collectionType: Class[_ <: util.Collection], elementClass: Class[T]): C
}