package com.github.learningwords.service

import java.io.{FileDescriptor, File, FileInputStream}

import _root_.android.app.{Notification, PendingIntent, Service}
import _root_.android.content.Intent
import _root_.android.media.MediaPlayer
import _root_.android.os._
import _root_.android.support.v4.content.LocalBroadcastManager
import _root_.android.util.Log
import com.github.learningwords._
import com.github.learningwords.activity.PlayerActivity

import scala.collection.mutable


class MediaPlayerService extends Service {


  private var tracks: List[(Long, Track)] = _
  private var playList: PlaylistDto = _
  private var mediaService: MediaService = _
  private var player: Player = _


  private var broadcaster: LocalBroadcastManager = _

  override def onCreate(): Unit = {
    super.onCreate()
    mediaService = MediaService(getApplicationContext)
    broadcaster = LocalBroadcastManager.getInstance(this)
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    if (intent.getAction.equals(MediaPlayerService.ACTION_START)) {
      // get playlist
      playList = intent.getSerializableExtra("playlist").asInstanceOf[PlaylistDto]
      tracks = playList.tracks.map(t => (t.id, t))
      var samples: List[Sample] = List()
      def createSample(t: (Long, Track)) = {
        samples = samples :+ new Sample(t._1, t._2.native.value, fileDescriptor(t._2.native).apply().get, playList.shortDelay.toInt)
        samples = samples :+ new Sample(t._1, t._2.foreign.value, fileDescriptor(t._2.foreign).apply().get, playList.longDelay.toInt)
      }
      tracks.foreach(createSample)
      player = new Player()
      player.setOnPlayListener(new OnPlayListener {
        override def onPlay(sample: Sample): Unit = {
          val intent = new Intent(classOf[MediaPlayerService].getCanonicalName)
          intent.putExtra(MediaPlayerService.CURRENT_TRACK, sample.id)
          intent.putExtra("last", samples.last.equals(sample))
          broadcaster.sendBroadcast(intent)
        }
      })
      player.samples = samples
      player.start()
      player.getLooper

    } else if (intent.getAction.equals(MediaPlayerService.ACTION_PAUSE)) {
      player.pause()
    } else if (intent.getAction.equals(MediaPlayerService.ACTION_PLAY)) {
      player.play()

    } else if (intent.getAction.equals(MediaPlayerService.ACTION_STOP)) {
      player.stopPlayer()

    }
    Service.START_NOT_STICKY // todo need investigation
  }

  private def setupMediaPlayer() = {
    // mediaPlayer.setWakeMode(getApplicationContext, PowerManager.PARTIAL_WAKE_LOCK)
  }

  override def onBind(intent: Intent): IBinder = null


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


  class Player(val name: String = classOf[Player].getName) extends HandlerThread(name) {
    var mHandler: PlayHandler = _
    var onPlayListener: Option[OnPlayListener] = None
    private var mediaPlayer: MediaPlayer = _
    var samples: List[Sample] = List()

    def setOnPlayListener(listener: OnPlayListener) = {
      onPlayListener = Some(listener)
    }

    override def onLooperPrepared(): Unit = {
      mHandler = new PlayHandler()
      mHandler.onPlayListener = onPlayListener
      samples.foreach(it => player.play(it))
    }


    def play(sample: Sample) {
      this.synchronized {
        mHandler
          .obtainMessage(MediaPlayerService.MESSAGE_WHAT_PLAY, sample)
          .sendToTarget()
      }

    }

    def pause(): Unit = {
      this.synchronized {
        //mHandler.pause()
        mHandler
          .obtainMessage(MediaPlayerService.MESSAGE_WHAT_PAUSE, None)
          .sendToTarget()
      }
    }

    def play(): Unit = {
      this.synchronized {
        //mHandler.play()
        mHandler
          .obtainMessage(MediaPlayerService.MESSAGE_WHAT_CONTINUE, None)
          .sendToTarget()
      }
    }

    def stopPlayer(): Unit = {
      quit()
    }
  }

  /**
   *
   */
  class PlayHandler extends Handler with MediaPlayer.OnPreparedListener
  with MediaPlayer.OnErrorListener with MediaPlayer.OnCompletionListener {
    private val messages = new mutable.Stack[Message]()
    var onPlayListener: Option[OnPlayListener] = None
    private var mediaPlayer: MediaPlayer = new MediaPlayer()
    val tag = classOf[PlayHandler].getCanonicalName
    private var length = 0

    @volatile private var suspended = false
    @volatile private var paused = false
    private var current: Sample = _

    {
      mediaPlayer.setOnCompletionListener(this)
      // mediaPlayer.setOnPreparedListener(this)
      mediaPlayer.setOnErrorListener(this)
    }

    private def suspend() {
      suspended = true
      Log.i(tag, "suspend")
    }

    private def resume() {
      Log.i(tag, "resume")
      suspended = false
      while (messages.nonEmpty) {
        sendMessageAtFrontOfQueue(messages.pop())
      }
    }


    @deprecated
    def play(): Unit = {
      if (paused) {
        paused = false
        mediaPlayer.seekTo(length)
        mediaPlayer.start()
      }
    }

    @deprecated
    def pause(): Unit = {
      suspend()
      paused = true
      mediaPlayer.pause()
      length = mediaPlayer.getCurrentPosition
    }

    override def handleMessage(msg: Message) {
      if (msg.what == MediaPlayerService.MESSAGE_WHAT_PAUSE) {
        paused = true
        mediaPlayer.pause()
        length = mediaPlayer.getCurrentPosition
        return
      }

      if (msg.what == MediaPlayerService.MESSAGE_WHAT_CONTINUE) {
        paused = false
        mediaPlayer.seekTo(length)
        mediaPlayer.start()
        return
      }

      if (suspended || paused) {
        messages.push(Message.obtain(msg))
        Log.i(tag, "push message to stack")
        return
      }

      if (msg.what == MediaPlayerService.MESSAGE_WHAT_PLAY) {
        val sample = msg.obj.asInstanceOf[Sample]
        if (onPlayListener.isDefined) {
          onPlayListener.get.onPlay(sample)
        }
        suspend()
        val name = sample.name
        current = sample
        mediaPlayer.setDataSource(sample.df)
        mediaPlayer.prepare()
        mediaPlayer.start()


        Log.i(tag, s"playing track: $name ")
      }
    }

    override def onPrepared(mp: MediaPlayer): Unit = {
      mp.start()
      Log.i(tag, "MediaPlayer start")
    }

    override def onCompletion(mp: MediaPlayer): Unit = {
      mp.stop()
      mp.reset()
      Log.i(tag, "MediaPlayer Complete")
      SystemClock.sleep(current.pause)
      resume()
    }

    override def onError(mp: MediaPlayer, what: Int, extra: Int): Boolean = {
      // todo
      // ... react appropriately ...
      // The MediaPlayer has moved to the Error state, must be reset!
      false
    }

  }

  class Sample(val id: Long, val name: String, val df: FileDescriptor, val pause: Int) {

  }

  trait OnPlayListener {
    def onPlay(sample: Sample)
  }

}


object MediaPlayerService {
  val ACTION_START = "mediaPlayerService.action.START"
  val ACTION_PAUSE = "mediaPlayerService.action.PAUSE"
  val ACTION_PLAY = "mediaPlayerService.action.PLAY"
  val ACTION_STOP = "mediaPlayerService.action.STOP"
  val CURRENT_TRACK = "currentTrack"
  val MESSAGE_WHAT_PLAY = 1
  val MESSAGE_WHAT_PAUSE = 2
  val MESSAGE_WHAT_CONTINUE = 3
}