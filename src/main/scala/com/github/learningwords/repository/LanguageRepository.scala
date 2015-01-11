package com.github.learningwords.repository


import com.github.learningwords.domain.Language
import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource
import scala.collection.JavaConverters._

import java.sql.SQLException


import com.j256.ormlite.table.TableUtils

/**
 * @author dmgcodevil
 */
@throws[SQLException]
class LanguageRepository(connectionSource: ConnectionSource,
                         dataClass: Class[Language]) extends BaseDaoImpl[Language, Long](connectionSource, dataClass) {


  /**
   * Gets all languages.
   * @throws SQLException
   * @return list of Language objects
   */
  @throws[SQLException]
  def getAllLanguages: List[Language] = {
    this.queryForAll().asScala.toList
  }

  def deleteAll(): Unit = {
    TableUtils.clearTable(connectionSource, dataClass)
  }

}
