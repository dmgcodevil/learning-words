package com.github.learningwords.basic.task.event

import com.google.common.eventbus.EventBus

/**
 * Created by rpl on 1/22/2015.
 */
object EventBusManager {
  val instance = new EventBusManager()


  def unregisterQuietly(obj: AnyRef, eventBus: EventBus): Boolean = {
    if (eventBus == null) {
      return false
    }
    try {
      // eventBus.unregister method throws llegalArgumentException
      // if the object was not previously registered or was already unregistered
      eventBus.unregister(obj)
      true
    } catch {
      case _: Exception => false
    }
  }
}

class EventBusManager {

  def store(key: String, eventBus: EventBus) {
    if (!eventBusMap.containsKey(key)) {
      eventBusMap.put(key, eventBus)
    }
  }

  def getEventBus(key: String): EventBus = {
    eventBusMap.get(key)
  }

  def removeEventBus(key: String): EventBus = {
    eventBusMap.remove(key)
  }

  private var eventBusMap: java.util.Map[String, EventBus] = new java.util.WeakHashMap[String, EventBus]


}

