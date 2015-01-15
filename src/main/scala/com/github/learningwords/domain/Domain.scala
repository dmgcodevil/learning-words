
package com.github.learningwords.domain

import com.j256.ormlite.field.DatabaseField

import scala.beans.BeanProperty

abstract class Domain {
  @DatabaseField
  @BeanProperty var id: Long = _
}