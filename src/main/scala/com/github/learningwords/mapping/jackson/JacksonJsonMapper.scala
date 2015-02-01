package com.github.learningwords.mapping.jackson

import java.io.{IOException, InputStream}

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.introspect.VisibilityChecker
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.github.learningwords.mapping.JsonMapper
import com.google.common.base.Throwables

class JacksonJsonMapper extends JsonMapper {

  private val objectMapper = new ObjectMapper()

  {
    objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig.getDefaultVisibilityChecker
      .withFieldVisibility(JsonAutoDetect.Visibility.ANY).asInstanceOf[VisibilityChecker[_]]
      .withGetterVisibility(JsonAutoDetect.Visibility.NONE).asInstanceOf[VisibilityChecker[_]]
      .withSetterVisibility(JsonAutoDetect.Visibility.NONE).asInstanceOf[VisibilityChecker[_]]
      .withCreatorVisibility(JsonAutoDetect.Visibility.NONE).asInstanceOf[VisibilityChecker[_]])
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

    val module = new SimpleModule()
    objectMapper.registerModule(module)
  }


  override def readObject[T](json: String, objType: Class[_ <: T]): T = {
    try {
      objectMapper.readValue(json, objType)
    } catch {
      case e: IOException => throw Throwables.propagate(e)
    }
  }


  override def readObject[T](is: InputStream, objType: Class[_ <: T]): T = {
    try {
      return objectMapper.readValue(is, objType)
    } catch {
      case e: IOException => throw Throwables.propagate(e)
    }
  }

}