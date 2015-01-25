package com.github.learningwords.basic.dialog


import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.{ViewGroup, LayoutInflater, View}
import android.widget.{ProgressBar, Button}
import com.github.learningwords.R
import com.github.learningwords.basic.task.event._

import com.google.common.eventbus.{Subscribe, EventBus}

/**
 * Fragment shows a task progress.
 */
object CustomProgressDialog {
  final val TAG_LOAD_PROGRESS_DIALOG: String = "task_fg"
}

class CustomProgressDialog extends DialogFragment {


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_progress, container, false)
    cancel = view.findViewById(R.id.cancel).asInstanceOf[Button]
    eventBus = EventUtils.getEventBus(this.getArguments)
    if (eventBus != null) {
      eventBus.register(this)
    }
    cancel.setOnClickListener(new View.OnClickListener {
      def onClick(v: View) {
        if (eventBus != null) {
          eventBus.post(new CancelEvent)
        }
        dismiss
      }
    })
    progressBar = view.findViewById(R.id.progressBar).asInstanceOf[ProgressBar]
    setCancelable(false)
    progressBar.setProgress(0)
    progressBar.setMax(100)
    return view
  }

  /**
   * This methods is invoked when a task has been completed.
   *
   * @param event the event
   */
  @Subscribe
  def onTaskComplete(event: CompleteEvent) {
    dismiss
  }

  @Subscribe
  def onTaskComplete(event: CancelledEvent) {
    dismiss
  }

  /**
   * Reacts on progress update events.
   *
   * @param event the update event
   */
  @Subscribe def onProgressUpdate(event: ProgressUpdateEvent) {
    progressBar.setProgress(event.getProgress)
  }

  override def onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    if (eventBus != null) {
      eventBus.unregister(this)
    }
  }

  private var cancel: Button = null
  private var progressBar: ProgressBar = null
  private var eventBus: EventBus = null
}