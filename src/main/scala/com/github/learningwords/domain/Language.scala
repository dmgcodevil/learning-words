package com.github.learningwords.domain

import com.j256.ormlite.field.{DataType, DatabaseField}
import com.j256.ormlite.table.DatabaseTable

/**
 * @author dmgcodevil
 */
@DatabaseTable(tableName = "language")
class Language(_name: String = null, _id: Long = 0, u: Unit = ()) {

  @DatabaseField(generatedId = true)
  var id: Long = _id

  @DatabaseField(canBeNull = false, dataType = DataType.STRING, columnName = "name")
  var name: String = _name

  def this() {
    this(u = ())
  }

  def this(_name: String) {
    this(_name, 0L, u = ())
  }
}