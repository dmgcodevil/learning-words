package com.github.learningwords.repository

import com.github.learningwords.domain.{Domain, Translation}
import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class TranslationRepository(connectionSource: ConnectionSource, dataClass: Class[Translation])
  extends BaseDaoImpl[Translation, Long](connectionSource, dataClass) with BaseRepository {

  val _connectionSource: ConnectionSource = connectionSource
  val _dataClass: Class[_ <: Domain] = dataClass
}

object TranslationRepository {
  def apply(connectionSource: ConnectionSource, dataClass: Class[Translation]): TranslationRepository = {
    new TranslationRepository(connectionSource, dataClass)
  }
}
