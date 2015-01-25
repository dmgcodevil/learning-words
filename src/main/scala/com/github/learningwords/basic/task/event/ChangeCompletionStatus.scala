package com.github.learningwords.basic.task.event

import com.github.learningwords.basic.task.event.TaskCompletionStatus.TaskCompletionStatus

/**
 * @author dmgcodevil
 */
class ChangeCompletionStatus {

  private var status: TaskCompletionStatus = TaskCompletionStatus.UNKNOWN

  def this(status: TaskCompletionStatus) {
    this()
    this.status = status
  }

  def getStatus = status

}

