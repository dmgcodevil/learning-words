package com.github.learningwords.basic.task.event

import android.os.Bundle
import com.google.common.eventbus.EventBus

/**
 * Created by rpl on 1/23/2015.
 */
object EventUtils {
  /**
   * Gets event bus from the fragment arguments.
   *
   * @return event bus
   */
  def getEventBus(bundle: Bundle): EventBus = {
    if (bundle != null && bundle.containsKey(EVENT_BUS)) {
      val key: String = bundle.getString(EVENT_BUS)
      EventBusManager.instance.getEventBus(key)
    }
    else {
      null
    }
  }

  val EVENT_BUS: String = "eventBus"
}


