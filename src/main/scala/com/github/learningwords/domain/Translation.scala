package com.github.learningwords.domain

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "translation")
class Translation extends Domain {
  @DatabaseField(foreign = true, columnName = Translation.FROM_ID_FIELD_NAME)
  var from: Word = _
  @DatabaseField(foreign = true, columnName = Translation.TO_ID_FIELD_NAME)
  var to: Word = _

  def this(from: Word, to: Word) {
    this()
    this.from = from
    this.to = to
  }
}

object Translation {
  final val FROM_ID_FIELD_NAME = "from_id"
  final val TO_ID_FIELD_NAME = "to_id"
}
