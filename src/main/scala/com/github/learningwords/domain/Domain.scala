
package com.github.learningwords.domain

import com.j256.ormlite.field.DatabaseField

import scala.beans.BeanProperty

abstract class Domain {
  @DatabaseField(generatedId = true, columnName = "id")
  @BeanProperty var id: Long = _
}
