package com.github.learningwords.fragment.basic

import com.github.learningwords.android.common.task.AsyncTask
import com.github.learningwords.android.common.task.TaskParams
import scala.collection.JavaConverters._
import scala.reflect.ClassTag

/**
 * Created by dmgcodevil on 1/20/2015.
 */
abstract class AsyncTaskUIAware[Params, Progress: ClassTag, Result] extends AsyncTask[Params, Progress, Result] {

  private var taskStateAware: AsyncTaskStateAware[Progress, Result] = null


  def setFragment(taskStateAware: AsyncTaskStateAware[Progress, Result]): Unit = {
    this.taskStateAware = taskStateAware
  }


  override def onProgressUpdate(progress: java.util.List[Progress]): Unit = {
    if (taskStateAware != null) {
      if (progress != null) {
        taskStateAware.updateProgress(progress.asScala.toArray)
      }

    }
  }

  def doPostExecute(result: Result)

  override def onPostExecute(result: Result) {
    doPostExecute(result)
    if (taskStateAware != null) {
      taskStateAware.taskFinished(result)
    }
  }

  def perform(params: TaskParams[Params]): Result

  override def doInBackground(params: TaskParams[Params]): Result = {
    if (!isCancelled) {
      perform(params)
    } else {
      null.asInstanceOf
    }
  }

}
