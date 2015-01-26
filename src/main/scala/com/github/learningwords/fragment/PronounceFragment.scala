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

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)
    mediaService = new MediaService(getActivity.getApplicationContext)
    if (getArguments != null) {
      word = getArguments.getSerializable(PronounceFragment.WORD).asInstanceOf[Word]
      progressDialogTag = CustomProgressDialog.TAG + getArguments.getString(PronounceFragment.ID)
      alertDialogTag = CustomAlertDialog.TAG + getArguments.getString(PronounceFragment.ID)
    }
  }


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
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
    task.subscribe(PronounceFragment.this)
    showLoadProgressFragment(task)
    task.execute()
  }

  private def showLoadProgressFragment(asyncTask: LoadPronunciationTask) {
    val fm: FragmentManager = getActivity.getFragmentManager
    val loadProgressFragment = CustomProgressDialog(asyncTask.key, "Loading pronunciation")
    asyncTask.subscribe(loadProgressFragment)
    loadProgressFragment.show(fm, progressDialogTag)
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


  class LoadPronunciationTask extends AsyncTask[Void, Integer, Void] {

    var key: String = UUID.randomUUID.toString
    var eventBus: EventBus = new EventBus
    var completionStatus: TaskCompletionStatus = TaskCompletionStatus.UNKNOWN

    {
      eventBus.register(this)
      EventBusManager.instance.store(key, eventBus)
    }

    def isSuccess = TaskCompletionStatus.SUCCESS.equals(completionStatus)

    def isFailed = TaskCompletionStatus.FAILED.equals(completionStatus)


    def subscribe(subscriber: AnyRef) {
      eventBus.register(subscriber)
    }

    override def onPreExecute(): Unit = {

    }

    override def onCancelled() {}

    @Subscribe def onCancelTaskEvent(cancelEvent: CancelTaskEvent) {
      cancel(cancelEvent.isMayInterruptIfRunning)
    }

    override def onPostExecute(ignore: Void) {
      eventBus.post(new CompleteEvent)
      if (isFailed) {
        createAlertDialog()
      }
      else {
        eventBus.unregister(this) // -> improve it
        EventBusManager.instance.removeEventBus(key) // -> improve it
        Toast.makeText(getActivity, "saved to " + Environment.getExternalStorageDirectory + "/pronunciation/" + word.lang.shortcut, Toast.LENGTH_LONG).show()
        if (mediaService.exists(word)) {
          playButton.setEnabled(true)
        }
      }
    }

    private def createAlertDialog() {
      val customAlertDialog = CustomAlertDialog(key, "Failed to download pronunciation", "Do you want to retry?")
      val bundle: Bundle = new Bundle
      val transaction: FragmentTransaction = getActivity.getFragmentManager.beginTransaction
      transaction.add(customAlertDialog, alertDialogTag)
      transaction.commitAllowingStateLoss
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

    override protected def onProgressUpdate(progresses: util.List[Integer]): Unit = {
    }
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
