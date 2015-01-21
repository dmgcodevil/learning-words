package com.github.learningwords.fragment


import java.io.{File, FileInputStream}

import android.app._
import android.content.{Context, DialogInterface}
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.{SystemClock, Bundle, Environment}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{ImageButton, Toast}
import com.github.learningwords.android.common.task.TaskParams
import com.github.learningwords.fragment.basic.{AsyncTaskUIAware, TaskFragment}
import com.github.learningwords.service.MediaService
import com.github.learningwords.service.pronunciation.{PronounceService, PronounceServiceType}
import com.github.learningwords.util.NetworkUtils
import com.github.learningwords.{R, Word}


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
  private var mProgressDialog: ProgressDialog = null
  private var activity: Activity = null;

  // Save a reference to the fragment manager. This is initialised in onCreate().
  private var mFM: FragmentManager = null


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    activity = getActivity
    setRetainInstance(true)

    mediaService = new MediaService(getActivity.getApplicationContext)



    if (getArguments != null) {
      word = getArguments.getSerializable(PronounceFragment.WORD).asInstanceOf[Word]
    }

    // At this point the fragment may have been recreated due to a rotation,
    // and there may be a TaskFragment lying around. So see if we can find it.
    mFM = getFragmentManager
    // Check to see if we have retained the worker fragment.
    val taskFragment = mFM.findFragmentByTag(PronounceFragment.TASK_FRAGMENT_TAG);

    if (taskFragment != null) {
      // Update the target fragment so it goes to this fragment instead of the old one.
      // This will also allow the GC to reclaim the old MainFragment, which the TaskFragment
      // keeps a reference to. Note that I looked in the code and setTargetFragment() doesn't
      // use weak references. To be sure you aren't leaking, you may wish to make your own
      // setTargetFragment() which does.
      taskFragment.setTargetFragment(this, PronounceFragment.TASK_FRAGMENT);
    }
  }


  //  override  def onSaveInstanceState(outState: Bundle) {
  //    //No call for super(). Bug on API Level > 11.
  //  }

  private def loadPronunciation(): Unit = {
    val taskFragment = new TaskFragment[Word, String]()
    taskFragment.title = "Loading pronunciation..."
    val downloadTsk = new DownloadPronouncationTask()
    taskFragment.setTask(downloadTsk)
    // tell it to call onActivityResult() on this fragment.
    taskFragment.setTargetFragment(PronounceFragment.this, PronounceFragment.TASK_FRAGMENT)
    // taskFragment.show(mFM, PronounceFragment.TASK_FRAGMENT_TAG)
    mFM.beginTransaction().add(taskFragment, PronounceFragment.TASK_FRAGMENT_TAG).commitAllowingStateLoss();
    downloadTsk.execute(Array(word): _*)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                            savedInstanceState: Bundle): View = {
    val view = inflater.inflate(R.layout.fragment_pronounce, container, false).asInstanceOf[View]
    playButton = view.findViewById(R.id.playButton).asInstanceOf[ImageButton]
    if (!mediaService.exists(word)) {
      playButton.setEnabled(false)
    } else {
      fileName = mediaService.buildFilePath(word)
    }
    downloadButton = view.findViewById(R.id.downloadBtn).asInstanceOf[ImageButton]
    openButton = view.findViewById(R.id.openBtn).asInstanceOf[ImageButton]
    recordButton = view.findViewById(R.id.addPronunciation).asInstanceOf[ImageButton]

    recordButton.setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View): Unit = {
        System.out.println()
      }
    })

    downloadButton.setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View): Unit = {
        if (!isNetworkAvailable) {
          showErrorDialog()

        } else {
          loadPronunciation()
        }
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
    view
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

  class DownloadPronouncationTask extends AsyncTaskUIAware[Word, Integer, String] {
    override def doPostExecute(result: String): Unit = {
      complete(result)
    }

    override def perform(params: TaskParams[Word]): String = {
      val param = params.getParams(0)
      SystemClock.sleep(1000) // for testing purposes
      try {
        val stream = pronounceService.getPronunciationAsStream(param.lang, param.value)
        fileName = mediaService.save(word, stream)
      } catch {
        case _: java.lang.Throwable => fileName = ""
      }
      fileName
    }
  }

  private def complete(result: String): String = {
    if (result.isEmpty) {
      new AlertDialog.Builder(activity)
        .setTitle("Failed to download pronunciation")
        .setMessage("Do you want to retry ?")
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        override def onClick(dialog: DialogInterface, which: Int) {
          loadPronunciation()
        }
      })
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
        override def onClick(dialog: DialogInterface, which: Int) {
          // do nothing
        }
      })
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show()
    } else {
      Toast.makeText(activity.getApplicationContext, "saved to " + Environment.getExternalStorageDirectory + "/pronunciation/" + word.lang.shortcut, Toast.LENGTH_LONG).show()
      playButton.setEnabled(true)
    }
    result
  }
}

object PronounceFragment {
  val WORD = "word"
  // Code to identify the fragment that is calling onActivityResult(). We don't really need
  // this since we only have one fragment to deal with.
  val TASK_FRAGMENT = 0

  // Tag so we can find the task fragment again, in another instance of this fragment after rotation.
  val TASK_FRAGMENT_TAG = "task"

  def apply(word: Word): PronounceFragment = {
    val fragment = new PronounceFragment()
    val args = new Bundle()
    args.putSerializable(WORD, word)
    fragment.setArguments(args)
    fragment
  }
}
