package com.github.learningwords.fragment.basic

/**
 * Created by dmgcodevil on 1/20/2015.
 */
trait AsyncTaskStateAware[Progress, Result] {

  def updateProgress(progress: Array[Progress])

  def taskFinished(result: Result)

}
