package com.github.learningwords.fragment.basic


import android.app.Activity
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar

import com.github.learningwords.R

import scala.reflect.ClassTag


/**
 * Created by dmgcodevil on 1/18/2015.
 */
class TaskFragment[Params: ClassTag, Result] extends DialogFragment with AsyncTaskStateAware[Integer, Result] {

  val TASK_FRAGMENT = 0

  // Tag so we can find the task fragment again, in another instance of this fragment after rotation.
  val TASK_FRAGMENT_TAG = "task"
  // The task we are running.
  private var asyncTask: AsyncTaskUIAware[Params, Integer, Result] = null
  private var parameters = Array[Params]()
  private var mProgressBar: ProgressBar = null
  private var onTaskFinishListener: OnTaskFinishListener[Result] = null


  def setTask(asyncTask: AsyncTaskUIAware[Params, Integer, Result]): Unit = {
    this.asyncTask = asyncTask
    // Tell the AsyncTask to call updateProgress() and taskFinished() on this fragment.
    asyncTask.setFragment(this)
  }

  def setParameters(parameters: Array[Params]): Unit = {
    this.parameters = parameters
  }

  def setOnTaskFinishListener(onTaskFinishListener: OnTaskFinishListener[Result]): Unit = {
    this.onTaskFinishListener = onTaskFinishListener
  }


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    // Retain this instance so it isn't destroyed when MainActivity and
    // MainFragment change configuration.
    setRetainInstance(true)

    // Start the task! You could move this outside this activity if you want.
    if (asyncTask != null) {
      if (parameters != null && parameters.length > 0) {
        asyncTask.execute(parameters: _*)
      } else {
        asyncTask.execute()
      }
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                            savedInstanceState: Bundle): View = {
    val view = inflater.inflate(R.layout.fragment_task, container)
    mProgressBar = view.findViewById(R.id.progressBarS).asInstanceOf[ProgressBar]

    getDialog.setTitle("Progress Dialog")


    // If you're doing a long task, you probably don't want people to cancel
    // it just by tapping the screen!
    getDialog.setCanceledOnTouchOutside(false)

    view
  }

  // This is to work around what is apparently a bug. If you don't have it
  // here the dialog will be dismissed on rotation, so tell it not to dismiss.
  override def onDestroyView(): Unit = {
    if (getDialog != null && getRetainInstance) {
      getDialog.setDismissMessage(null)
    }
    super.onDestroyView()
  }

  // Also when we are dismissed we need to cancel the task.
  override def onDismiss(dialog: DialogInterface): Unit = {
    super.onDismiss(dialog)
    // If true, the thread is interrupted immediately, which may do bad things.
    // If false, it guarantees a result is never returned (onPostExecute() isn't called)
    // but you have to repeatedly call isCancelled() in your doInBackground()
    // function to check if it should exit. For some tasks that might not be feasible.
    if (asyncTask != null) {
      asyncTask.cancel(false)
    }

    // You don't really need this if you don't want.
    if (getTargetFragment != null) {
      getTargetFragment.onActivityResult(TASK_FRAGMENT, Activity.RESULT_CANCELED, null)
    }

  }

  override def onResume(): Unit = {
    super.onResume()
    // This is a little hacky, but we will see if the task has finished while we weren't
    // in this activity, and then we can dismiss ourselves.
    if (asyncTask == null) {
      dismiss()
    }

  }

  // This is called by the AsyncTask.
  override def updateProgress(integers: Array[Integer]) {
    if (integers != null && integers.length > 0) {
      var percent = integers(0)
      if (percent == null) {
        // todo change to Option
        percent = 0
      }
      mProgressBar.setProgress(percent)
    }
  }

  // This is also called by the AsyncTask.
  override def taskFinished(result: Result) {
    // Make sure we check if it is resumed because we will crash if trying to dismiss the dialog
    // after the user has switched to another app.
    if (isResumed) {
      dismiss()
    }

    // If we aren't resumed, setting the task to null will allow us to dimiss ourselves in
    // onResume().
    asyncTask = null

    // Tell the fragment that we are done.
    if (getTargetFragment != null) {
      getTargetFragment.onActivityResult(TASK_FRAGMENT, Activity.RESULT_OK, null)
    }
    if (onTaskFinishListener != null) {
      onTaskFinishListener.onTaskFinish(result)
    }

  }

}
