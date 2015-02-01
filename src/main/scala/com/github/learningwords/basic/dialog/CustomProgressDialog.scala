package com.github.learningwords.basic.dialog


import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.{ViewGroup, LayoutInflater, View}
import android.widget.{TextView, ProgressBar, Button}
import com.github.learningwords.R
import com.github.learningwords.basic.task.event._

import com.google.common.eventbus.{Subscribe, EventBus}

/**
 * Fragment shows a task progress.
 */
object CustomProgressDialog {
  val TAG: String = "progress_dialog"
  val TITLE: String = "title"

  def apply(eventBusKey: String, title: String = "Processing..."): CustomProgressDialog = {
    val dialog = new CustomProgressDialog()
    val bundle = new Bundle()
    bundle.putString(EventUtils.EVENT_BUS, eventBusKey)
    bundle.putString(TITLE, title)
    dialog.setArguments(bundle)
    dialog
  }

}

class CustomProgressDialog extends DialogFragment {

  private var cancel: Button = null
  private var dialogLabel: TextView = null
  private var progressBar: ProgressBar = null
  private var eventBus: EventBus = null


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_progress, container, false)
    cancel = view.findViewById(R.id.cancel).asInstanceOf[Button]
    dialogLabel = view.findViewById(R.id.dialogLabel).asInstanceOf[TextView]
    dialogLabel.setText(getTitle)
    eventBus = EventUtils.getEventBus(getArguments)
    cancel.setOnClickListener(new View.OnClickListener {
      def onClick(v: View) {
        if (eventBus != null) {
          eventBus.post(new CancelTaskEvent)
        }
        dismiss()
      }
    })
    progressBar = view.findViewById(R.id.progressBar).asInstanceOf[ProgressBar]
    progressBar.setProgress(0)
    progressBar.setMax(100)
    view
  }

  /**
   * This methods is invoked when a task has been completed.
   *
   * @param event the event
   */
  @Subscribe
  def onTaskComplete(event: CompleteEvent) {
    dismiss()
  }

  /**
   * Reacts on progress update events.
   *
   * @param event the update event
   */
  @Subscribe def onProgressUpdate(event: ProgressUpdateEvent) {
    progressBar.setProgress(event.getProgress)
  }

  /**
   * This method can be invoked because of following reasons:
   * 1. Change configuration: rotate
   * 2. direct invocation of "dismiss method"
   *
   * @param dialog
   */
  override def onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    // if this method was triggered because of changing configuration then we need to unregister
    // this dialog from event bus
    unregister()
  }

  override def onCancel(dialog: DialogInterface): Unit = {
    super.onCancel(dialog)
    if (eventBus != null) {
      eventBus.post(new CancelTaskEvent)
      EventBusManager.unregisterQuietly(CustomProgressDialog.this, eventBus)
    }
  }

  private def unregister(): Unit = {
    if (eventBus != null) {
      EventBusManager.unregisterQuietly(CustomProgressDialog.this, eventBus)
    }
  }

  private def getTitle: String = {
    if (getArguments != null)
      getArguments.getString(CustomProgressDialog.TITLE)
    else null
  }
}