package com.github.learningwords.app

import android.app.Application
import com.github.learningwords.repository.util.HelperFactory

class LearningWordsApplication extends Application {
  override def onCreate() {
    super.onCreate()
    HelperFactory.setHelper(this)
  }
}
