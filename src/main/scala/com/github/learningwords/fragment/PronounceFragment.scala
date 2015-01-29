package com.github.learningwords.fragment


import java.io.{File, FileInputStream}
import java.util
import java.util.UUID

import android.app._
import android.content.Intent
import android.media.MediaPlayer
import android.os.{Bundle, Environment, SystemClock}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget._
import com.github.learningwords.activity.AudioRecordActivity
import com.github.learningwords.android.common.task.{AsyncTask, TaskParams}
import com.github.learningwords.basic.dialog.{CustomAlertDialog, CustomProgressDialog}
import com.github.learningwords.basic.task.event.TaskCompletionStatus.TaskCompletionStatus
import com.github.learningwords.basic.task.event._
import com.github.learningwords.service.MediaService
import com.github.learningwords.service.pronunciation.{PronounceService, PronounceServiceType}
import com.github.learningwords.{R, Word}
import com.google.common.eventbus.{EventBus, Subscribe}


class PronounceFragment extends Fragment {


  private var word: Word = null

  private var recordButton: ImageButton = null
  private var downloadButton: ImageButton = null
  private var openButton: ImageButton = null
  private var playButton: ImageButton = null
  private val pronounceService = PronounceService(PronounceServiceType.Soundoftext)
  private val mp = new MediaPlayer()
  private var mediaService: MediaService = null
  //private var fileName: String = null
  private var task: LoadPronunciationTask = null
  private var progressDialogTag = ""
  private var alertDialogTag = ""
  val eventBusHolder = UUID.randomUUID().toString -> new EventBus()

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)

    EventBusManager.instance.store(eventBusHolder._1, eventBusHolder._2)
    eventBusHolder._2.register(PronounceFragment.this)

    mediaService = new MediaService(getActivity.getApplicationContext)
    if (getArguments != null) {
      word = getArguments.getSerializable(PronounceFragment.WORD).asInstanceOf[Word]
      progressDialogTag = CustomProgressDialog.TAG + getArguments.getString(PronounceFragment.ID)
      alertDialogTag = CustomAlertDialog.TAG + getArguments.getString(PronounceFragment.ID)
    }
  }


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    removeIfExists(progressDialogTag)
    removeIfExists(alertDialogTag)
    showDialog();
    val view: View = inflater.inflate(R.layout.fragment_pronounce, container, false)

    playButton = view.findViewById(R.id.playButton).asInstanceOf[ImageButton]
    if (mediaService.exists(word)) {
      playButton.setEnabled(true)
    } else {
      playButton.setEnabled(false)
    }
    openButton = view.findViewById(R.id.openBtn).asInstanceOf[ImageButton]
    recordButton = view.findViewById(R.id.addPronunciation).asInstanceOf[ImageButton]
    recordButton.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = {
        val intent = new Intent(getActivity.getApplicationContext, classOf[AudioRecordActivity])
        startActivity(intent)
      }
    })

    downloadButton = view.findViewById(R.id.downloadBtn).asInstanceOf[ImageButton]
    downloadButton.setOnClickListener(new View.OnClickListener {
      def onClick(v: View) {
        startTask()
      }
    })

    playButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View): Unit = {
        if (mediaService.exists(word)) {
          mp.reset()
          val fis = new FileInputStream(new File(mediaService.getFilePath(word)))
          mp.setDataSource(fis.getFD)
          mp.prepare()
          mp.start()
        }
      }
    })

    view
  }

  def removeIfExists(fragmentTag: String) {
    val fm: FragmentManager = getActivity.getFragmentManager
    val fragment = fm.findFragmentByTag(fragmentTag);
    if (fragment != null) {
      fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
    }
  }

  def showDialog(): Unit = {
    if (task != null) {
      val fm: FragmentManager = getActivity.getFragmentManager
      if (android.os.AsyncTask.Status.RUNNING.equals(task.getStatus) || android.os.AsyncTask.Status.PENDING.equals(task.getStatus)) {
        val loadProgressFragment = CustomProgressDialog(eventBusHolder._1, "Loading pronunciation")
        eventBusHolder._2.register(loadProgressFragment)
        loadProgressFragment.show(fm, progressDialogTag)
      } else {
        if (TaskCompletionStatus.FAILED.equals(task.completionStatus)) {
          showAlertDialog()
        }
      }
    }
  }

  def showAlertDialog(): Unit = {
    val fm: FragmentManager = getActivity.getFragmentManager
    val customAlertDialog = CustomAlertDialog(eventBusHolder._1, "Failed to download pronunciation", "Do you want to retry?")
    val transaction: FragmentTransaction = getActivity.getFragmentManager.beginTransaction
    transaction.add(customAlertDialog, alertDialogTag)
    transaction.commitAllowingStateLoss()
    eventBusHolder._2.register(customAlertDialog)
  }

  //  def showAlertDialogOnLost(): Unit = {
  //    val customAlertDialog = CustomAlertDialog(eventBusHolder._1, "Failed to download pronunciation", "Do you want to retry?")
  //    val transaction: FragmentTransaction = getActivity.getFragmentManager.beginTransaction
  //    transaction.add(customAlertDialog, alertDialogTag)
  //    transaction.commit()
  //
  //  }

  @Subscribe def startTask(startTaskEvent: StartTaskEvent) {
    startTask()
  }

  @Subscribe def onAlertDialogAnswerNoEvent(event: AlertDialogAnswerNoEvent) {
    task = null
  }

  @Subscribe def onAlertDialogAnswerYesEvent(event: AlertDialogAnswerYesEvent) {
    startTask()
  }


  private def startTask() {
    task = new LoadPronunciationTask()
    task.execute()
    showDialog()
  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    //hack
  }

  @Subscribe def onCancelTaskEvent(cancelEvent: CancelTaskEvent) {
    if (task != null) {
      task.cancel(cancelEvent.isMayInterruptIfRunning)
      task = null
    }
  }

  // вызывается при смене ориентации экрана перпед doDetach
  override def onDestroyView(): Unit = {
    super.onDestroyView()
  }

  // вызывается при смене ориентации экрана после onDestroyView
  override def onDetach(): Unit = {
    super.onDetach()
  }

  // это метод вызывается когда нажимаем назад
  override def onDestroy(): Unit = {
    super.onDestroy()
    EventBusManager.instance.removeEventBus(eventBusHolder._1)
    try {
      eventBusHolder._2.unregister(PronounceFragment.this)
    } catch {
      case _: Exception => {}
    }
  }

  class LoadPronunciationTask extends AsyncTask[Void, Integer, Void] {

    var completionStatus: TaskCompletionStatus = TaskCompletionStatus.UNKNOWN;

    override def onPreExecute(): Unit = {

    }

    override def onCancelled(): Unit = {
      removeIfExists(progressDialogTag)
      removeIfExists(alertDialogTag)
    }

    override def onPostExecute(ignore: Void) {
      removeIfExists(progressDialogTag)
      removeIfExists(alertDialogTag)

      if (TaskCompletionStatus.FAILED.equals(task.completionStatus)) {
        showAlertDialog()
      } else {
        Toast.makeText(getActivity, "saved to " + Environment.getExternalStorageDirectory + "/pronunciation/" + word.lang.shortcut, Toast.LENGTH_LONG).show()
      }
    }


    override protected def doInBackground(params: TaskParams[Void]): Void = {
      try {
        SystemClock.sleep(3000) // for testing purposes
        val stream = pronounceService.getPronunciationAsStream(word.lang, word.value)
        mediaService.save(word, stream)
        completionStatus = TaskCompletionStatus.SUCCESS
      }
      catch {
        case e: Exception => {
          completionStatus = TaskCompletionStatus.FAILED
        }
      }
      null
    }

    override protected def onProgressUpdate(progresses: util.List[Integer]): Unit = {}
  }

}

object PronounceFragment {
  val WORD = "word"
  val ID = "id"

  def apply(word: Word, id: String): PronounceFragment = {
    val fragment = new PronounceFragment()
    val bundle = new Bundle()
    bundle.putSerializable(WORD, word)
    bundle.putString(ID, id)
    fragment.setArguments(bundle)
    fragment
  }
}
