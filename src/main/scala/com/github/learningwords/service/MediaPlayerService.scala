package com.github.learningwords.service

import java.io.{File, FileInputStream}

import _root_.android.app.{Notification, PendingIntent, Service}
import _root_.android.content.Intent
import _root_.android.media.MediaPlayer
import _root_.android.os.IBinder
import com.github.learningwords._
import com.github.learningwords.activity.PlayerActivity

class MediaPlayerService extends Service with MediaPlayer.OnPreparedListener
with MediaPlayer.OnErrorListener with MediaPlayer.OnCompletionListener {


  private var mediaPlayer: MediaPlayer = _
  private var tracks: List[(Long, Track)] = _
  private var playList: PlaylistDto = _
  private var mediaService: MediaService = _
  private var currentTrack: (Long, Track) = _
  private var pos: Int = 0
  private var first: Boolean = false

  override def onCreate(): Unit = {
    super.onCreate()
    mediaService = MediaService(getApplicationContext)
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    if (intent.getAction.equals(MediaPlayerService.ACTION_PLAY)) {
      // get playlist
      playList = intent.getSerializableExtra("playlist").asInstanceOf[PlaylistDto]

      tracks = playList.tracks.map(t => (t.id, t))
      mediaPlayer = new MediaPlayer()
      setupMediaPlayer()
      mediaPlayer.setOnPreparedListener(this)
      mediaPlayer.setOnCompletionListener(this)
      // take fist track to play
      currentTrack = tracks(pos)
      mediaPlayer.reset()
      setDataSource(currentTrack._2)

      first = true
      mediaPlayer.prepareAsync() // prepare async to not block main thread
    }
    Service.START_NOT_STICKY // todo need investigation
  }

  private def setupMediaPlayer() = {
    // mediaPlayer.setWakeMode(getApplicationContext, PowerManager.PARTIAL_WAKE_LOCK)
  }

  override def onBind(intent: Intent): IBinder = null

  override def onPrepared(mp: MediaPlayer): Unit = {
    // take fist track to play
    mediaPlayer.start()
  }

  private def setDataSource(track: Track): Unit = {
    val fd = fileDescriptor(track.native).apply()
    if (fd.isEmpty) {
      throw new RuntimeException("failed to get file descriptor")
    }
    mediaPlayer.setDataSource(fd.get)
    first = true
  }

  //  private def play(): Unit = {
  //    mediaPlayer.start()
  //  }

  private def play(word: WordDto): Unit = {
    mediaPlayer.reset()
    val fd = fileDescriptor(word).apply()
    if (fd.isEmpty) {
      throw new RuntimeException("failed to get file descriptor")
    }
    mediaPlayer.setDataSource(fd.get)
    mediaPlayer.prepareAsync()
  }

  override def onError(mp: MediaPlayer, what: Int, extra: Int): Boolean = {
    // todo
    // ... react appropriately ...
    // The MediaPlayer has moved to the Error state, must be reset!
    false
  }

  private def showNotification(songName: String) = {

    // assign the song name to songName
    val originalIntent = new Intent(getApplicationContext, classOf[PlayerActivity]) // replace direct declaration
    val pi = PendingIntent.getActivity(getApplicationContext, 0, originalIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    val notification = new Notification()
    notification.tickerText = "test"
    notification.icon = R.drawable.play
    notification.flags |= Notification.FLAG_ONGOING_EVENT;
    notification.setLatestEventInfo(getApplicationContext, "MediaPlayerService",
      "Playing: " + songName, pi)
    startForeground(1, notification)
  }

  private def fileDescriptor(word: WordDto) = () => {
    try {
      val is = new FileInputStream(new File(mediaService.getFilePath(word)))
      Some(is.getFD)
      //IOUtils.closeQuietly(is)
      //res
    } catch {
      case ex: Exception => None
    }
  }

  override def onCompletion(mp: MediaPlayer): Unit = {
    if (first) {
      // play silence
      play(currentTrack._2.foreign)
      first = false
    } else {
      if (pos < tracks.size - 1) {
        pos += 1
        currentTrack = tracks(pos)
        first = true
        play(currentTrack._2.native)

      }

    }
  }
}


object MediaPlayerService {
  val ACTION_PLAY = "mediaPlayerService.action.PLAY"
}