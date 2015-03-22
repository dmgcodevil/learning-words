package com.github.learningwords.app

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.github.learningwords.repository.util.HelperFactory

class LearningWordsApplication extends Application {
  override def onCreate() {
    super.onCreate()
    val context = this
    new Runnable() {
      override def run = {
        // TODO: call release somewhere
        HelperFactory.setHelper(context)
      }
    }.run
  }

  // solve multidex problems
  override protected def attachBaseContext(base: Context) = {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }
}
