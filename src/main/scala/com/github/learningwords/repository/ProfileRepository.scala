package com.github.learningwords.repository


import java.sql.SQLException

import com.github.learningwords.domain.{Domain, Profile}
import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

/**
 * @author dmgcodevil
 */
@throws[SQLException]
class ProfileRepository(connectionSource: ConnectionSource, dataClass: Class[Profile])
  extends BaseDaoImpl[Profile, Long](connectionSource, dataClass) with BaseRepository {

  val _connectionSource: ConnectionSource = connectionSource
  val _dataClass: Class[_ <: Domain] = dataClass

  /**
   * Gets all languages.
   * @throws SQLException
   * @return list of Language objects
   */
  @throws[SQLException]
  def getProfile: Profile = {
    val elements = this.queryForAll()
    if (elements != null && !elements.isEmpty) elements.get(0) else null
  }


}

object ProfileRepository {
  def apply(conn: ConnectionSource, dataType: Class[Profile]): ProfileRepository = {
    new ProfileRepository(conn, dataType)
  }

}
