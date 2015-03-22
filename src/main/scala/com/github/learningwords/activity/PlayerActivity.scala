package com.github.learningwords.activity

import java.io.Serializable
import java.nio.CharBuffer
import java.nio.charset.Charset

import _root_.android.app.Activity
import _root_.android.content.{IntentFilter, BroadcastReceiver, Context, Intent}
import _root_.android.graphics.BitmapFactory
import _root_.android.os.Bundle
import _root_.android.support.v4.content.LocalBroadcastManager
import _root_.android.view.Menu
import _root_.android.view.MenuItem
import _root_.android.widget._

import com.github.learningwords.basic.api.Buttons
import com.github.learningwords.service.MediaPlayerService
import com.github.learningwords.view.adapter.TrackAdapter


import com.github.learningwords._

class PlayerActivity extends Activity with PlaybackListener {

  private var nativeWordText: TextView = _
  private var foreignWordText: TextView = _
  private var prevTrackBtn: Button = _
  private var playTrackBtn: Button = _
  private var nextTrackBtn: Button = _
  private var trackList: ListView = _
  private var pSeekBar: SeekBar = _
  private val ruLang = new Language("russian", "ru")
  private val enLang = new Language("english", "en")
  private var receiver: BroadcastReceiver = _
  private var currentTrack: TrackDto = _

  private var currentState: PlaybackState = StateStop

  def playOperation = currentState.play


  // todo mock
  var tracks = List[TrackDto]()

  var playList: Playlist = _
  var playbackConf: PlaybackConfig = _

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
    pSeekBar = findViewById(R.id.pSeekBar).asInstanceOf[SeekBar]
    pSeekBar.setMax(playList.size)
    trackList.setChoiceMode(1); // ListView.CHOICE_MODE_SINGLE

    val adapter = new TrackAdapter(this, tracks)
    trackList.setAdapter(adapter)
    def startMediaService() {
      playOperation
      //      if (!play) {
      //        val intent = new Intent(PlayerActivity.this, classOf[MediaPlayerService])
      //        intent.setAction(MediaPlayerService.ACTION_START)
      //        intent.putExtra("playlist", playList.slice.asInstanceOf[Serializable])
      //        startService(intent)
      //        play = true
      //        playTrackBtn.setText("||")
      //        return
      //      }
      //      if (!paused) {
      //        paused = true
      //        playTrackBtn.setText(">")
      //        val intent = new Intent(PlayerActivity.this, classOf[MediaPlayerService])
      //        intent.setAction(MediaPlayerService.ACTION_PAUSE)
      //        startService(intent)
      //        return
      //      }
      //      if (paused) {
      //        playTrackBtn.setText("||")
      //        paused = false
      //        val intent = new Intent(PlayerActivity.this, classOf[MediaPlayerService])
      //        intent.setAction(MediaPlayerService.ACTION_PLAY)
      //        startService(intent)
      //        return
      //      }


    }
    Buttons.setOnClick(playTrackBtn, startMediaService)
    Buttons.setOnClick(nextTrackBtn, nextTrack)
    receiver = new BroadcastReceiver() {
      override def onReceive(context: Context, intent: Intent) = {
        val eventType = intent.getSerializableExtra(MediaPlayerService.EVENT).asInstanceOf[String]

        // val last = intent.getSerializableExtra("last").asInstanceOf[Boolean]
        // val first = intent.getSerializableExtra("first").asInstanceOf[Boolean]
        //currentTrack = playList.tracks.find(t => t.id == id).get
        if (MediaPlayerService.EVENT_START_TRACK_PLAYBACK.equals(eventType)) {
          val id = intent.getSerializableExtra("id").asInstanceOf[Long]
          playList.next
        }
        if (MediaPlayerService.EVENT_PLAYBACK_COMPLETED.equals(eventType)) {
          playList.reset()
        }
      }
    }
  }

  private def nextTrack(): Unit = {
    //    val index = playList.tracks.indexOf(currentTrack) + 1
    //    val newPlaylist = new PlaylistDto(playList.tracks.slice(index, playList.tracks.size))
    //    newPlaylist.longDelay = 230L
    //    newPlaylist.shortDelay = 100L
    //
    //    val intent = new Intent(PlayerActivity.this, classOf[MediaPlayerService])
    //    intent.setAction(MediaPlayerService.ACTION_START)
    //    intent.putExtra("playlist", newPlaylist)
    //    startService(intent)
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

  // for testing
  private def preparePlaylist(native: String, foreign: String) {
    val natives = native.split(",")
    val foreigns = foreign.split(",")
    for (i <- 0 until natives.length) {
      tracks = tracks :+ new TrackDto(i.toLong,
        new WordDto(ruLang, natives(i).trim),
        new WordDto(enLang, foreigns(i).trim))
    }
    playList = new Playlist(tracks)
    playbackConf = new PlaybackConfig(shortDelay = 100L, longDelay = 230L)
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

  override def onNext(pos: Int, track: TrackDto): Unit = {
    nativeWordText.setText(track.native.value)
    foreignWordText.setText(track.foreign.value)
    pSeekBar.setProgress(pos)
  }

  override def onReset(): Unit = {

  }

  override def onSwitch(pos: Int, track: TrackDto): Unit = {

  }

  trait PlaybackState {
    def play
  }

  private object StateStop extends PlaybackState {
    override def play: Unit = {
      playList.reset()
      val intent = new Intent(PlayerActivity.this, classOf[MediaPlayerService])
      intent.setAction(MediaPlayerService.ACTION_START)
      intent.putExtra(MediaPlayerService.TRACKS, playList.tracks.asInstanceOf[Serializable])
      intent.putExtra(MediaPlayerService.PLAYBACK_CONFIG, playbackConf.asInstanceOf[Serializable])
      startService(intent)
      playTrackBtn.setText("||")
      currentState = StatePlaying
    }
  }

  private object StatePlaying extends PlaybackState {
    override def play: Unit = {
      playTrackBtn.setText(">")
      val intent = new Intent(PlayerActivity.this, classOf[MediaPlayerService])
      intent.setAction(MediaPlayerService.ACTION_PAUSE)
      startService(intent)
      return
    }
  }

  private object StatePause extends PlaybackState {
    override def play: Unit = {
      playTrackBtn.setText("||")
      val intent = new Intent(PlayerActivity.this, classOf[MediaPlayerService])
      intent.setAction(MediaPlayerService.ACTION_PLAY)
      startService(intent)
    }
  }

}
