package com.github.learningwords.basic.dialog

import android.app.{Activity, AlertDialog, Dialog, DialogFragment}
import android.content.DialogInterface
import android.os.Bundle
import com.github.learningwords.basic.task.event.{EventUtils, TaskCompletionStatus, ChangeCompletionStatus, StartTaskEvent}
import com.google.common.eventbus.EventBus

/**
 * Alert dialog that allows interact with any backend via event bus.
 */
object CustomAlertDialog {
  var TAG: String = "alertDialog"
}

class CustomAlertDialog extends DialogFragment {
  override def onAttach(activity: Activity) {
    super.onAttach(activity)
  }

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    eventBus = EventUtils.getEventBus(this.getArguments)
    if (eventBus != null) {
      eventBus.register(this)
    }
    return new AlertDialog.Builder(getActivity).setTitle("asd").setMessage("sad").setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        if (eventBus != null) {
          eventBus.post(new StartTaskEvent)
        }
      }
    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        if (eventBus != null) {
          eventBus.post(new ChangeCompletionStatus(TaskCompletionStatus.SUCCESS))
        }
      }
    }).create
  }

  override def onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    if (eventBus != null) {
      eventBus.unregister(this)
    }
  }

  private var eventBus: EventBus = null
}

