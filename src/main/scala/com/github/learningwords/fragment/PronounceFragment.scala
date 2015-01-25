package com.github.learningwords.fragment


import java.io.{File, FileInputStream}
import java.util
import java.util.UUID

import android.app._
import android.content.{Context, DialogInterface}
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.{SystemClock, Bundle, Environment}
import com.github.learningwords.android.common.task.{TaskParams, AsyncTask}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget._
import com.github.learningwords.basic.dialog.{CustomProgressDialog, CustomAlertDialog}
import com.github.learningwords.basic.task.event.TaskCompletionStatus.TaskCompletionStatus
import com.github.learningwords.basic.task.event._
import com.github.learningwords.service.MediaService
import com.github.learningwords.service.pronunciation.{PronounceService, PronounceServiceType}
import com.github.learningwords.util.NetworkUtils
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
  private var fileName: String = null
  private var task: LoadPronunciationTask = null


  // Save a reference to the fragment manager. This is initialised in onCreate().
  private var mFM: FragmentManager = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)

    mediaService = new MediaService(getActivity.getApplicationContext)

    if (getArguments != null) {
      word = getArguments.getSerializable(PronounceFragment.WORD).asInstanceOf[Word]
    }


  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view: View = inflater.inflate(R.layout.fragment_pronounce, container, false)


    if (task != null && (task.getStatus == android.os.AsyncTask.Status.RUNNING) && task.isSuccess) {
      showLoadProgressFragment(task)
    }
    if (task != null && task.isFailed) {
      if (getActivity.getFragmentManager.findFragmentByTag(CustomAlertDialog.TAG) == null) {
        val customAlertDialog: CustomAlertDialog = new CustomAlertDialog
        val bundle: Bundle = new Bundle
        bundle.putSerializable(EventUtils.EVENT_BUS, task.getKey)
        customAlertDialog.setArguments(bundle)
        val transaction: FragmentTransaction = getActivity.getFragmentManager.beginTransaction
        transaction.add(customAlertDialog, CustomAlertDialog.TAG)
        transaction.commit
      }
    }
    playButton = view.findViewById(R.id.playButton).asInstanceOf[ImageButton]
    if (!mediaService.exists(word)) {
      playButton.setEnabled(false)
    } else {
      fileName = mediaService.buildFilePath(word)
    }
    downloadButton = view.findViewById(R.id.downloadBtn).asInstanceOf[ImageButton]
    downloadButton.setOnClickListener(new View.OnClickListener {
      def onClick(v: View) {
        startTask
      }
    })
    openButton = view.findViewById(R.id.openBtn).asInstanceOf[ImageButton]
    recordButton = view.findViewById(R.id.addPronunciation).asInstanceOf[ImageButton]

    recordButton.setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View): Unit = {
        System.out.println()
      }
    })
    playButton.setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View): Unit = {
        if (fileName != null) {
          mp.reset()
          val fis = new FileInputStream(new File(fileName))
          mp.setDataSource(fis.getFD)
          mp.prepare()
          mp.start()
        }
      }
    })

    return view
  }

  @Subscribe def startTask(startTaskEvent: StartTaskEvent) {
    startTask
  }

  override def onResume {
    super.onResume
    if (task != null && (task.getStatus == android.os.AsyncTask.Status.RUNNING) && task.isSuccess) {
      showLoadProgressFragment(task)
    }
  }

  private def startTask {
    if (task != null) {
      EventBusManager.instance.removeEventBus(task.getKey)
    }
    task = new LoadPronunciationTask()
    task.subscribe(PronounceFragment.this)
    showLoadProgressFragment(task)
    task.execute()
  }

  private def showLoadProgressFragment(asyncTask: LoadPronunciationTask) {
    val fm: FragmentManager = getActivity.getFragmentManager
    val loadProgressFragment: CustomProgressDialog = new CustomProgressDialog
    val bundle: Bundle = new Bundle
    bundle.putSerializable(EventUtils.EVENT_BUS, asyncTask.getKey)
    loadProgressFragment.setArguments(bundle)
    asyncTask.subscribe(loadProgressFragment)
    loadProgressFragment.show(fm, CustomProgressDialog.TAG_LOAD_PROGRESS_DIALOG)
  }

  override def onAttach(activity: Activity) {
    super.onAttach(activity)
  }

  override def onDetach {
    super.onDetach
  }

  override def onPause {
    super.onPause
    val fragment: Fragment = getActivity.getFragmentManager.findFragmentByTag(CustomProgressDialog.TAG_LOAD_PROGRESS_DIALOG)
    if (fragment != null) {
      getActivity.getFragmentManager.beginTransaction.remove(fragment).commitAllowingStateLoss
    }

    val alertDialog: Fragment = getActivity.getFragmentManager.findFragmentByTag(CustomAlertDialog.TAG)
    if (alertDialog != null) {
      getActivity.getFragmentManager.beginTransaction.remove(alertDialog).commitAllowingStateLoss
    }
  }

  override def onDestroy {
    super.onDestroy
    if (task != null && (task.getStatus == android.os.AsyncTask.Status.FINISHED)) {
      EventBusManager.instance.removeEventBus(task.getKey)
    }
  }

  override def onSaveInstanceState(outState: Bundle) {
  }

  private def isNetworkAvailable = NetworkUtils.isNetworkAvailable(
    getActivity.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager])


  private def showErrorDialog(): Unit = {

    new AlertDialog.Builder(getActivity)
      .setTitle("No internet connection")
      .setMessage("Please check your internet connection")
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, which: Int): Unit = {
        // todo what to do ?
      }
    })
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show()
  }

  private def exists(word: Word): Boolean = {
    false // todo implement it
  }


  class LoadPronunciationTask extends AsyncTask[Void, Integer, Void] {

    private var key: String = UUID.randomUUID.toString
    private var eventBus: EventBus = new EventBus
    private var completionStatus: TaskCompletionStatus = TaskCompletionStatus.UNKNOWN

    {
      eventBus.register(LoadPronunciationTask.this)
      EventBusManager.instance.store(key, eventBus)
    }

    def isSuccess: Boolean = {
      TaskCompletionStatus.SUCCESS.equals(completionStatus) || TaskCompletionStatus.UNKNOWN.equals(completionStatus)
    }

    def isFailed: Boolean = {
      TaskCompletionStatus.FAILED.equals(completionStatus)
    }


    def getKey = key

    def getEventBus = eventBus

    def subscribe(subscriber: AnyRef) {
      eventBus.register(subscriber)
    }

    protected override def onPreExecute {
      //textView.setText("start loading")
    }

    /**
     * Note that we do NOT call the callback object's methods
     * directly from the background thread, as this could result
     * in a race condition.
     */


    override def onCancelled {
      //textView.setText("canceled")
      eventBus.post(new CancelledEvent)
      EventBusManager.instance.removeEventBus(key)
    }

    @Subscribe def onCancelEvent(cancelEvent: CancelEvent) {
      cancel(cancelEvent.isMayInterruptIfRunning)
    }

    @Subscribe def onChangeCompletionStatus(event: ChangeCompletionStatus) {
      completionStatus = event.getStatus
    }

    override def onPostExecute(ignore: Void) {
      // textView.setText("done")
      eventBus.post(new CompleteEvent)
      if (isFailed) {
        createAlertDialog
      }
      else {
        eventBus.unregister(this)
        EventBusManager.instance.removeEventBus(key)
        Toast.makeText(getActivity, "saved to " + Environment.getExternalStorageDirectory + "/pronunciation/" + word.lang.shortcut, Toast.LENGTH_LONG).show()
        playButton.setEnabled(true)
      }
    }

    private def createAlertDialog {
      val customAlertDialog: CustomAlertDialog = new CustomAlertDialog
      val bundle: Bundle = new Bundle
      bundle.putSerializable(EventUtils.EVENT_BUS, key)
      customAlertDialog.setArguments(bundle)
      val transaction: FragmentTransaction = getActivity.getFragmentManager.beginTransaction
      transaction.add(customAlertDialog, CustomAlertDialog.TAG)
      transaction.commitAllowingStateLoss
    }


    override protected def doInBackground(params: TaskParams[Void]): Void = {

      try {
        SystemClock.sleep(3000) // for testing purposes
        val stream = pronounceService.getPronunciationAsStream(word.lang, word.value)
        fileName = mediaService.save(word, stream)
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
      // val progressUpdateEvent: ProgressUpdateEvent = new ProgressUpdateEvent(progresses.get(0))
      //textView.setText("in progress...")
      // eventBus.post(progressUpdateEvent)
    }
  }


}

object PronounceFragment {
  val WORD = "word"

  def apply(word: Word): PronounceFragment = {
    val fragment = new PronounceFragment()
    val args = new Bundle()
    args.putSerializable(WORD, word)
    fragment.setArguments(args)
    fragment
  }
}
