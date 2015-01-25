package com.github.learningwords.basic.task.event

/**
 * @author dmgcodevil
 */
object TaskCompletionStatus extends Enumeration {
  type TaskCompletionStatus = Value
  val UNKNOWN, SUCCESS, FAILED = Value
}


