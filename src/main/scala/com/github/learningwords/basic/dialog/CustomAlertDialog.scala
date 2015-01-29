package com.github.learningwords.basic.dialog

import android.app.{Activity, AlertDialog, Dialog, DialogFragment}
import android.content.DialogInterface
import android.os.Bundle
import com.github.learningwords.basic.task.event._
import com.google.common.eventbus.EventBus

/**
 * Alert dialog that allows interact with any backend via event bus.
 */
object CustomAlertDialog {
  var TAG: String = "alert_dialog"
  var TITLE = "title"
  var MESSAGE = "message"

  def apply(eventBusKey: String, title: String = "title", message: String = "message"): CustomAlertDialog = {
    val dialog = new CustomAlertDialog()
    val bundle = new Bundle()
    bundle.putString(EventUtils.EVENT_BUS, eventBusKey)
    bundle.putString(TITLE, title)
    bundle.putString(MESSAGE, message)
    dialog.setArguments(bundle)
    dialog
  }
}

class CustomAlertDialog extends DialogFragment {

  private var eventBus: EventBus = null

  override def onAttach(activity: Activity) {
    super.onAttach(activity)
  }

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    eventBus = EventUtils.getEventBus(getArguments)
//    if (eventBus != null) {
//      eventBus.register(this)
//    }
    def getTitle: String = {
      if (getArguments != null)
        getArguments.getString(CustomAlertDialog.TITLE)
      else null
    }

    def getMessage: String = {
      if (getArguments != null)
        getArguments.getString(CustomAlertDialog.MESSAGE)
      else null
    }
    new AlertDialog.Builder(getActivity).setTitle(getTitle).setMessage(getMessage)
      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        if (eventBus != null) {
          eventBus.post(new AlertDialogAnswerYesEvent())
          dismiss()
        }
      }
    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        if (eventBus != null) {
          eventBus.post(new AlertDialogAnswerNoEvent())
          dismiss()
        }
      }
    }).create
  }

  override def onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    unregister()
  }


  override def onCancel(dialog: DialogInterface): Unit = {
    super.onCancel(dialog)
    if (eventBus != null) {
      eventBus.post(new AlertDialogAnswerNoEvent())
    }
    unregister()
  }

  private def unregister(): Unit = {
    if (eventBus != null) {
      try {
        eventBus.unregister(CustomAlertDialog.this)
      } catch {
        case e: Exception => println(e.getMessage)
      }

    }
  }



}

