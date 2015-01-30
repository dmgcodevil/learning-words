package com.test.learningwords.runner

import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.test.InstrumentationTestRunner;

// solve multidex problems
class MultiDexTestRunner extends InstrumentationTestRunner {
  override def onCreate(arguments: Bundle) {
    MultiDex.install(getTargetContext());
    super.onCreate(arguments);
  }
}
