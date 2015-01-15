package com.github.learningwords.repository

import com.github.learningwords.domain.Domain
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils

trait BaseRepository {

  val _connectionSource: ConnectionSource
  val _dataClass: Class[_ <: Domain]

  def deleteAll(): Unit = {
    TableUtils.clearTable(_connectionSource, _dataClass)
  }

}