package com.github.learningwords.activity

import java.io.{File, FileInputStream, IOException}

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.{MediaPlayer, MediaRecorder}
import android.os.Bundle
import android.util.Log
import android.view.{Menu, MenuItem, View}
import android.widget.ImageButton
import com.github.learningwords.{R, Word}
import com.github.learningwords.service.MediaService
import com.google.common.base.Throwables


class AudioRecordActivity extends Activity {

  private var mFileName: String = null

  private var mRecordButton: ImageButton = null
  private var mStartRecording: Boolean = true

  private var mPlayButton: ImageButton = null
  private var mStartPlaying: Boolean = true

  private var mRecorder: MediaRecorder = null
  private var mPlayer: MediaPlayer = null
  private var mediaService: MediaService = null
  private var save: ImageButton = null
  private var cancel: ImageButton = null
  private var word: Word = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_audio_record)
    mediaService = MediaService.apply(getApplicationContext)
    mFileName = mediaService.getPronunciationRecordFolder + "/temp.mp3"
    word = getIntent.getExtras.getSerializable("word").asInstanceOf[Word]
    save = findViewById(R.id.saveRecordedPronunciation).asInstanceOf[ImageButton]
    save.setEnabled(false)
    save.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) = {
        val returnIntent = new Intent()
        try {
          mediaService.save(word, new FileInputStream(new File(mFileName)))
          val file = new File(mFileName)
          file.delete();
        } catch {
          case e: Exception => throw Throwables.propagate(e)

        }
        returnIntent.putExtra("result", true)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
      }
    })
    cancel = findViewById(R.id.cancelRecord).asInstanceOf[ImageButton]
    cancel.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {
        val returnIntent = new Intent()
        returnIntent.putExtra("result", false)
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
      }
    })

    mPlayButton = findViewById(R.id.playRecord).asInstanceOf[ImageButton]
    mRecordButton = findViewById(R.id.recordVoice).asInstanceOf[ImageButton]
    mPlayButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) = {
        onPlay(mStartPlaying)
        if (mStartPlaying) {
          mPlayButton.setImageBitmap(BitmapFactory.decodeResource(getResources, R.drawable.stop))
        } else {
          mPlayButton.setImageBitmap(BitmapFactory.decodeResource(getResources, R.drawable.play))
        }
        mStartPlaying = !mStartPlaying
      }
    })

    mRecordButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {
        onRecord(mStartRecording)
        if (mStartRecording) {
          mRecordButton.setImageBitmap(BitmapFactory.decodeResource(getResources, R.drawable.stop))
        } else {
          mRecordButton.setImageBitmap(BitmapFactory.decodeResource(getResources, R.drawable.record))
        }
        mStartRecording = !mStartRecording
      }
    })
  }

  private def onRecord(start: Boolean) = {
    if (start) {
      startRecording()
    } else {
      stopRecording()
    }
  }

  private def startRecording() {
    mRecorder = new MediaRecorder()
    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
    mRecorder.setOutputFile(mFileName)
    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

    try {
      mRecorder.prepare()
    } catch {
      case e: IOException => Log.e(classOf[AudioRecordActivity].getName, "prepare() failed")
    }

    mRecorder.start()
  }

  private def stopRecording() = {
    mRecorder.stop()
    mRecorder.release()
    mRecorder = null
    save.setEnabled(true)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.menu_audio_record, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.getItemId

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true
    }

    super.onOptionsItemSelected(item)
  }

  private def onPlay(start: Boolean) {
    if (start) {
      startPlaying()
    } else {
      stopPlaying()
    }
  }

  private def startPlaying() = {
    mPlayer = new MediaPlayer()
    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      override def onCompletion(mp: MediaPlayer) {
        mPlayButton.setImageBitmap(BitmapFactory.decodeResource(getResources, R.drawable.play))
      }
    })
    try {
      mPlayer.setDataSource(mFileName)
      mPlayer.prepare()
      mPlayer.start()
    } catch {
      case e: IOException => Log.e(classOf[AudioRecordActivity].getName, "prepare() failed")
    }
  }

  private def stopPlaying() = {
    mPlayer.release()
    mPlayer = null
  }
}
