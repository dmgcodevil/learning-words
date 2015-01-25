package com.github.learningwords.basic.task.event

/**
 * Used to notifies a subscribers about a task progress.
 *
 * @author dmgcodevil
 */
class ProgressUpdateEvent {

  private var progress: Int = 0

  def this(progress: Int) {
    this()
    this.progress = progress
  }

  def getProgress = progress


}

