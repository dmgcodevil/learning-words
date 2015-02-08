package com.github.learningwords.activity

import java.nio.CharBuffer
import java.nio.charset.Charset

import _root_.android.app.Activity
import _root_.android.content.Intent
import _root_.android.os.Bundle
import _root_.android.view.Menu
import _root_.android.view.MenuItem
import _root_.android.widget.{ListView, Button, EditText}
import com.github.learningwords.basic.api.Buttons
import com.github.learningwords.service.MediaPlayerService
import com.github.learningwords.view.adapter.TrackAdapter

import com.github.learningwords._


class PlayerActivity extends Activity {

  private var nativeWordText: EditText = _
  private var foreignWordText: EditText = _
  private var prevTrackBtn: Button = _
  private var playTrackBtn: Button = _
  private var nextTrackBtn: Button = _
  private var trackList: ListView = _
  private val ruLang = new Language("russian", "ru")
  private val enLang = new Language("english", "en")

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
    nativeWordText = findViewById(R.id.nativeWordText).asInstanceOf[EditText]
    foreignWordText = findViewById(R.id.foreignWordText).asInstanceOf[EditText]
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
  }

  private def preparePlaylist(native: String, foreign: String) {
    val natives = native.split(",")
    val foreigns = foreign.split(",")
    for (i <- 0 until natives.length) {
      tracks = tracks :+ new Track(0L, new WordDto(ruLang, natives(i).trim), new WordDto(enLang, foreigns(i).trim))
    }
    playList = new PlaylistDto(tracks)
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
