package com.github.learningwords.fragment


import java.io.{IOException, File, FileInputStream, InputStream}

import android.app._
import android.content.{Context, DialogInterface}
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.{Bundle, Environment}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{ImageButton, ProgressBar, Toast}
import com.github.learningwords.android.common.task.{TaskParams, AsyncTask}
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
    setRetainInstance(true)

    mediaService = new MediaService(getActivity.getApplicationContext)



    if (getArguments != null) {
      word = getArguments.getSerializable(PronounceFragment.WORD).asInstanceOf[Word]
    }

    // At this point the fragment may have been recreated due to a rotation,
    // and there may be a TaskFragment lying around. So see if we can find it.
    mFM = getFragmentManager();
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

  //  override def onAttach( activity: Activity)=
  //  {
  //    super.onAttach(activity);
  //    if (!(activity instanceof Callbacks))
  //    {
  //      throw new IllegalStateException("Activity must implement fragment's callbacks.");
  //    }
  //    mCallbacks = (Callbacks) activity;
  //  }
  //
  //  @Override
  //  public void onDetach()
  //  {
  //    super.onDetach();
  //    mCallbacks = sDummyCallbacks;
  //  }


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


activity = getActivity();


    downloadButton.setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View): Unit = {
        //        if (!isNetworkAvailable) {
        //          showErrorDialog()
        //
        //        } else {
        //          mProgressDialog = ProgressDialog.show(getActivity, "Load pronunciation", "loading...", true, true)
        //          val task = new DownloadPronouncationTask().execute(word)
        //          mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener {
        //            override def onCancel(dialog: DialogInterface): Unit = {
        //              task.cancel(true)
        //            }
        //          })
        //        }

        val taskFragment = new TaskFragment();
        // And create a task for it to monitor. In this implementation the taskFragment
        // executes the task, but you could change it so that it is started here.
        taskFragment.setTask(new MyTask(new Action[String] {
          override def doAction(): String = {
            val stream = pronounceService.getPronunciationAsStream(word.lang, word.value)
            fileName = mediaService.save(word, stream)
            fileName
          }

          override def onComplete(): Unit = {
            Toast.makeText(activity.getApplicationContext, "saved to " + Environment.getExternalStorageDirectory + "/pronunciation/" + word.lang.shortcut, Toast.LENGTH_LONG).show()
          }
        }));
        // And tell it to call onActivityResult() on this fragment.
        taskFragment.setTargetFragment(PronounceFragment.this, PronounceFragment.TASK_FRAGMENT);

        // Show the fragment.
        // I'm not sure which of the following two lines is best to use but this one works well.
        taskFragment.show(mFM, PronounceFragment.TASK_FRAGMENT_TAG);
        //      mFM.beginTransaction().add(taskFragment, TASK_FRAGMENT_TAG).commit();
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

  class DownloadPronouncationTask extends AsyncTask[Word, Unit, String] {

    override def onPostExecute(result: String): Unit = {
      super.onPostExecute(result)
      complete(result)
    }

    override def onCancelled(): Unit = {
      super.onCancelled()
      fileName = null
      if (!mediaService.exists(word)) {
        playButton.setEnabled(false)
      }
    }

    override def onCancelled(result: String): Unit = {
      super.onCancelled(result)
      complete(result)
    }

    override protected def doInBackground(params: TaskParams[Word]): String = {
      Thread.sleep(3000)
      val param = params.getParams(0)
      val stream = pronounceService.getPronunciationAsStream(param.lang, param.value)
      fileName = mediaService.save(word, stream)
      fileName
    }
  }

  private def complete(result: String): String = {
    Toast.makeText(getActivity.getApplicationContext, "saved to " + Environment.getExternalStorageDirectory + "/pronunciation/" + word.lang.shortcut, Toast.LENGTH_LONG).show()
    mProgressDialog.dismiss()
    playButton.setEnabled(true)
    result
  }
}

object PronounceFragment {
  val WORD = "word"
  // Code to identify the fragment that is calling onActivityResult(). We don't really need
  // this since we only have one fragment to deal with.
  val TASK_FRAGMENT = 0;

  // Tag so we can find the task fragment again, in another instance of this fragment after rotation.
  val TASK_FRAGMENT_TAG = "task";

  def apply(word: Word): PronounceFragment = {
    val fragment = new PronounceFragment()
    val args = new Bundle()
    args.putSerializable(WORD, word)
    fragment.setArguments(args)
    fragment
  }
}
