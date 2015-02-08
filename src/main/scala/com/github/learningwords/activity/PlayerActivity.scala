package com.github.learningwords.activity

import java.nio.CharBuffer
import java.nio.charset.Charset

import _root_.android.app.Activity
import _root_.android.content.{IntentFilter, BroadcastReceiver, Context, Intent}
import _root_.android.os.Bundle
import _root_.android.support.v4.content.LocalBroadcastManager
import _root_.android.view.Menu
import _root_.android.view.MenuItem
import _root_.android.widget.{TextView, ListView, Button, EditText}
import com.github.learningwords.basic.api.Buttons
import com.github.learningwords.service.MediaPlayerService
import com.github.learningwords.view.adapter.TrackAdapter

import com.github.learningwords._


class PlayerActivity extends Activity {

  private var nativeWordText: TextView = _
  private var foreignWordText: TextView = _
  private var prevTrackBtn: Button = _
  private var playTrackBtn: Button = _
  private var nextTrackBtn: Button = _
  private var trackList: ListView = _
  private val ruLang = new Language("russian", "ru")
  private val enLang = new Language("english", "en")
  private var receiver: BroadcastReceiver = _

  // todo mock
  var tracks = List[Track]()

  var playList: PlaylistDto = _

  private def encode(source: String): String = {
    new String(source.getBytes, Charset.forName("UTF-16"))
    //UTF-16
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    val nativeWords = getIntent.getStringExtra("nativeWords")
    val foreignWords = getIntent.getStringExtra("foreignWords")
    preparePlaylist(nativeWords, foreignWords)
    setContentView(R.layout.activity_player)
    nativeWordText = findViewById(R.id.nativeWordText).asInstanceOf[TextView]
    foreignWordText = findViewById(R.id.foreignWordText).asInstanceOf[TextView]
    prevTrackBtn = findViewById(R.id.prevTrackBtn).asInstanceOf[Button]
    playTrackBtn = findViewById(R.id.playTrackBtn).asInstanceOf[Button]
    nextTrackBtn = findViewById(R.id.nextTrackBtn).asInstanceOf[Button]
    trackList = findViewById(R.id.trackList).asInstanceOf[ListView]
    val adapter = new TrackAdapter(this, tracks)
    trackList.setAdapter(adapter)
    def startMediaService(): Unit = {
      val intent = new Intent(this, classOf[MediaPlayerService])
      intent.setAction(MediaPlayerService.ACTION_PLAY)
      intent.putExtra("playlist", playList)
      startService(intent)
    }
    Buttons.setOnClick(playTrackBtn, startMediaService)

    receiver = new BroadcastReceiver() {
      override def onReceive(context: Context, intent: Intent) = {
        val track = intent.getSerializableExtra(MediaPlayerService.CURRENT_TRACK).asInstanceOf[Track]
        nativeWordText.setText(track.native.value)
        foreignWordText.setText(track.foreign.value)
        trackList.setSelection(playList.tracks.indexWhere(t => track.id.equals(t.id)))
        // do something here.
      }
    }
  }


  override def onStart(): Unit = {
    super.onStart()
    LocalBroadcastManager.getInstance(this)
      .registerReceiver(receiver, new IntentFilter(classOf[MediaPlayerService].getCanonicalName))
  }

  override def onStop(): Unit = {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    super.onStop()
  }

  private def preparePlaylist(native: String, foreign: String) {
    val natives = native.split(",")
    val foreigns = foreign.split(",")
    for (i <- 0 until natives.length) {
      tracks = tracks :+ new Track(0L, new WordDto(ruLang, natives(i).trim), new WordDto(enLang, foreigns(i).trim))
    }
    playList = new PlaylistDto(tracks)
    playList.longDelay = 230L
    playList.shortDelay = 100L
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.menu_player, menu)
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
}
