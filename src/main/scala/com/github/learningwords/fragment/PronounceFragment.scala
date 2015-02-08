package com.github.learningwords.fragment


import java.io.{File, FileInputStream}
import java.util
import java.util.UUID

import android.app._
import android.content.{ActivityNotFoundException, Intent}
import android.media.MediaPlayer
import android.os.{Bundle, SystemClock}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget._
import com.github.learningwords.activity.AudioRecordActivity
import com.github.learningwords.android.common.task.{AsyncTask, TaskParams}
import com.github.learningwords.basic.dialog.{CustomAlertDialog, CustomProgressDialog}
import com.github.learningwords.basic.task.event.TaskCompletionStatus.TaskCompletionStatus
import com.github.learningwords.basic.task.event._
import com.github.learningwords.service.MediaService
import com.github.learningwords.service.pronunciation.{PronounceService, PronounceServiceType}
import com.github.learningwords.util.FileUtils
import com.github.learningwords.{R, WordDto}
import com.google.common.eventbus.{EventBus, Subscribe}


class PronounceFragment extends Fragment {


  private var word: WordDto = null

  private var recordButton: ImageButton = null
  private var downloadButton: ImageButton = null
  private var openButton: ImageButton = null
  private var playButton: ImageButton = null
  private val pronounceService = PronounceService(PronounceServiceType.Soundoftext)
  private val mp = new MediaPlayer()
  private var mediaService: MediaService = null
  private var task: LoadPronunciationTask = null
  private var progressDialogTag = ""
  private var alertDialogTag = ""
  private val eventBus = new EventBus()
  private val eventBusHolder = UUID.randomUUID().toString -> eventBus
  private val recordingResultCode = 100
  private val fileSelectCode = 110

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)

    EventBusManager.instance.store(eventBusHolder._1, eventBusHolder._2)
    eventBusHolder._2.register(PronounceFragment.this)
    mediaService = MediaService(getActivity.getApplicationContext)
    if (getArguments != null) {
      word = getArguments.getSerializable(PronounceFragment.WORD).asInstanceOf[WordDto]
      progressDialogTag = CustomProgressDialog.TAG + getArguments.getString(PronounceFragment.ID)
      alertDialogTag = CustomAlertDialog.TAG + getArguments.getString(PronounceFragment.ID)
    }
  }


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    destroyDialogs()
    showDialog()
    val view: View = inflater.inflate(R.layout.fragment_pronounce, container, false)
    playButton = view.findViewById(R.id.playButton).asInstanceOf[ImageButton]
    if (mediaService.exists(word)) {
      playButton.setEnabled(true)
    } else {
      playButton.setEnabled(false)
    }
    openButton = view.findViewById(R.id.openBtn).asInstanceOf[ImageButton]
    openButton.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = {
        showFileChooser()
      }
    })
    recordButton = view.findViewById(R.id.addPronunciation).asInstanceOf[ImageButton]
    recordButton.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = {
        val intent = new Intent(getActivity.getApplicationContext, classOf[AudioRecordActivity])
        intent.putExtra("word", word)
        startActivityForResult(intent, recordingResultCode)
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

  def removeFragment(fragmentTag: String) {
    val fm: FragmentManager = getActivity.getFragmentManager
    val fragment = fm.findFragmentByTag(fragmentTag)
    if (fragment != null) {
      fm.beginTransaction().remove(fragment).commitAllowingStateLoss()
    }
  }

  def removeFragment(fragmentTags: Array[String]) {
    def isNotNull(f: AnyRef): Boolean = f != null
    val fm: FragmentManager = getActivity.getFragmentManager
    val fragments = fragmentTags.map(tag => fm.findFragmentByTag(tag)).filter(isNotNull)
    if (fragments.nonEmpty) {
      def remove(tr: FragmentTransaction): FragmentTransaction = {
        fragments.foreach(f => tr.remove(f))
        tr
      }
      def commit(tr: FragmentTransaction) = tr.commitAllowingStateLoss()
      commit(remove(fm.beginTransaction()))
    }
  }

  def showDialog(): Unit = {
    if (task != null) {
      val fm: FragmentManager = getActivity.getFragmentManager
      if (android.os.AsyncTask.Status.RUNNING.equals(task.getStatus)
        || android.os.AsyncTask.Status.PENDING.equals(task.getStatus)) {
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

  private def enablePlayButton(): Unit = {
    if (mediaService.exists(word)) {
      playButton.setEnabled(true)
    } else {
      playButton.setEnabled(false)
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

  private def showFileChooser(): Unit = {
    val intent = new Intent(Intent.ACTION_GET_CONTENT)
    intent.setType("*/*")
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    try {
      startActivityForResult(
        Intent.createChooser(intent, "Select a File to Upload"), fileSelectCode);
    } catch {
      case ex: ActivityNotFoundException =>
        // Potentially direct the user to the Market with a Dialog
        Toast.makeText(getActivity, "Please install a File Manager.", Toast.LENGTH_LONG).show()
    }
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

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    (requestCode, resultCode) match {
      case (`recordingResultCode`, Activity.RESULT_OK) => {
        Toast.makeText(getActivity, "successfully recorded", Toast.LENGTH_SHORT).show()
        enablePlayButton()
      }
      case (`recordingResultCode`, Activity.RESULT_CANCELED) => {
        enablePlayButton()
      }
      case (`fileSelectCode`, Activity.RESULT_OK) => {
        val uri = data.getData
        // Get the path
        val path = FileUtils.getPath(getActivity, uri)
        if (path != null) {
          mediaService.save(word, new FileInputStream(new File(path)))
          enablePlayButton()
          Toast.makeText(getActivity, "successfully added", Toast.LENGTH_SHORT).show()
        }
      }
      case _ => {}
    }
  }

  override def onDestroyView(): Unit = {
    super.onDestroyView()
  }

  private def destroyDialogs(): Unit = {
    //removeFragment(progressDialogTag)
    //removeFragment(alertDialogTag)
    removeFragment(Array(progressDialogTag, alertDialogTag))
  }

  override def onDetach(): Unit = {
    super.onDetach()
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    EventBusManager.instance.removeEventBus(eventBusHolder._1)
    EventBusManager.unregisterQuietly(PronounceFragment.this, eventBusHolder._2)
  }

  class LoadPronunciationTask extends AsyncTask[Void, Integer, Void] {
    var completionStatus: TaskCompletionStatus = TaskCompletionStatus.UNKNOWN

    override def onPreExecute(): Unit = {}

    override def onCancelled(): Unit = {
      destroyDialogs()
    }

    override def onPostExecute(ignore: Void) {
      destroyDialogs()

      if (TaskCompletionStatus.FAILED.equals(task.completionStatus)) {
        showAlertDialog()
      } else {
        Toast.makeText(getActivity, "saved to " + mediaService.getFilePath(word), Toast.LENGTH_LONG).show()
      }
    }


    override protected def doInBackground(params: TaskParams[Void]): Void = {
      try {
        SystemClock.sleep(1000) // for testing purposes
        val stream = pronounceService.getPronunciationAsStream(word.lang, word.value)
        mediaService.save(word, stream)
        completionStatus = TaskCompletionStatus.SUCCESS
        enablePlayButton()
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
          completionStatus = TaskCompletionStatus.FAILED
      }
      null
    }

    override protected def onProgressUpdate(progresses: util.List[Integer]): Unit = {}
  }

}

object PronounceFragment {
  val WORD = "word"
  val ID = "id"

  def apply(word: WordDto, id: String): PronounceFragment = {
    val fragment = new PronounceFragment()
    val bundle = new Bundle()
    bundle.putSerializable(WORD, word)
    bundle.putString(ID, id)
    fragment.setArguments(bundle)
    fragment
  }
}
