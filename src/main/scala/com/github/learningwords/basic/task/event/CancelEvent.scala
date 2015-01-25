package com.github.learningwords.basic.task.event

/**
 * This event is used to cancel a task, see {@link android.os.AsyncTask#cancel(boolean)}.
 *
 * @author dmgcodevil
 */
class CancelEvent {

  private var mayInterruptIfRunning: Boolean = false

  def this(mayInterruptIfRunning: Boolean) {
    this()
    this.mayInterruptIfRunning = mayInterruptIfRunning
  }

  def isMayInterruptIfRunning = mayInterruptIfRunning

}