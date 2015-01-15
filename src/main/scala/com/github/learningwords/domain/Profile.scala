package com.github.learningwords.domain

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import scala.beans.BeanProperty

@DatabaseTable(tableName = "profile")
class Profile extends Domain {

  @DatabaseField
  @BeanProperty var nativeLang: String = _

  def this(nativeLang: String) {
    this()
    this.nativeLang = nativeLang
  }

}