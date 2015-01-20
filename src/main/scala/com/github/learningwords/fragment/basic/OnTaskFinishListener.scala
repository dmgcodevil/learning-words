package com.github.learningwords.fragment.basic

/**
 * Created by dmgcodevil on 1/20/2015.
 */
trait OnTaskFinishListener[R] {

  def onTaskFinish(result: R)

}
