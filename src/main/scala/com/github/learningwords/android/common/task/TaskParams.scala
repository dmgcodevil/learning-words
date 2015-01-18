package com.github.learningwords.android.common.task

import scala.collection.JavaConverters._

class TaskParams[Params] {

  private var params: List[Params] = List.empty[Params];

  def this(params: List[Params]) {
    this()
    this.params = params
  }

  def this(params: java.util.List[Params]) {
    this(params.asScala.toList)
  }


  def getParams: List[Params] = params
}