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
  private var samples: List[Sample] = _
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
      samples = List()
      def createSample(t: (Long, Track)) = {
        samples = samples :+ new Sample(t._1, t._2.native.value, fileDescriptor(t._2.native).apply().get, playList.shortDelay.toInt)
        samples = samples :+ new Sample(t._1, t._2.foreign.value, fileDescriptor(t._2.foreign).apply().get, playList.longDelay.toInt, false)
      }
      tracks.foreach(createSample)
      play()

    } else if (intent.getAction.equals(MediaPlayerService.ACTION_PAUSE)) {
      player.pause()
    } else if (intent.getAction.equals(MediaPlayerService.ACTION_PLAY)) {
      player.play()

    }
    Service.START_NOT_STICKY // todo need investigation
  }

  private def play(): Unit = {
    if (player == null) {
      player = new Player()
      player.setOnPlayListener(new OnPlayListener {
        override def onStart(sample: Sample): Unit = {
          notifyActivity(sample, MediaPlayerService.EVENT_START_PLAYBACK)
        }

        override def onComplete(sample: Sample): Unit = {
          notifyActivity(sample, MediaPlayerService.EVENT_COMPLETE_PLAYBACK)
        }
      })
      player.samples = samples
      player.start()
      player.getLooper
    } else {
      player.stopPlayer()
      player.play(samples)
    }
  }

  //  override def stopService(name: Intent): Boolean = {
  //    // TODO Auto-generated method stub
  //    player.stopPlayer()
  //    player.quit()
  //    super.stopService(name)
  //  }

  private def notifyActivity(sample: Sample, event: String): Unit = {
    val intent = new Intent(classOf[MediaPlayerService].getCanonicalName)
    intent.putExtra("eventType", event)
    intent.putExtra("id", sample.id)
    intent.putExtra("last", samples.last.equals(sample))
    intent.putExtra("first", sample.first)
    broadcaster.sendBroadcast(intent)
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
      samples.foreach(play)
    }

    def play(pSamples: List[Sample]) {
      samples = pSamples
      samples.foreach(play)
    }

    def play(sample: Sample) {
      mHandler
        .obtainMessage(MediaPlayerService.MESSAGE_PLAY, sample)
        .sendToTarget()
    }

    def pause(): Unit = {
      mHandler
        .obtainMessage(MediaPlayerService.MESSAGE_PAUSE, None)
        .sendToTarget()
    }

    def play(): Unit = {
      mHandler
        .obtainMessage(MediaPlayerService.MESSAGE_CONTINUE, None)
        .sendToTarget()
    }

    def stopPlayer(): Unit = {
      mHandler
        .obtainMessage(MediaPlayerService.MESSAGE_STOP, None)
        .sendToTarget()
      mHandler = new PlayHandler()
      mHandler.onPlayListener = onPlayListener
    }
  }

  /**
   * The handler that handles messages {@link MediaPlayerService.Sample} to play them.
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

    override def handleMessage(msg: Message) {
      if (msg.what == MediaPlayerService.MESSAGE_STOP) {
        removeMessages(MediaPlayerService.MESSAGE_PLAY)
        suspended = false
        paused = false
        mediaPlayer.stop()
        mediaPlayer.reset()
        return
      }

      if (msg.what == MediaPlayerService.MESSAGE_PAUSE) {
        paused = true
        mediaPlayer.pause()
        length = mediaPlayer.getCurrentPosition
        return
      }

      if (msg.what == MediaPlayerService.MESSAGE_CONTINUE) {
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

      if (msg.what == MediaPlayerService.MESSAGE_PLAY) {
        val sample = msg.obj.asInstanceOf[Sample]

        suspend()
        val name = sample.name
        current = sample
        mediaPlayer.setDataSource(sample.df)
        mediaPlayer.prepare()
        if (onPlayListener.isDefined) {
          onPlayListener.get.onStart(sample)
        }
        mediaPlayer.start()
        Log.i(tag, s"start playback of the sample: $name ")
      }
    }

    override def onPrepared(mp: MediaPlayer): Unit = {
      mp.start()
      Log.i(tag, "MediaPlayer start")
    }

    override def onCompletion(mp: MediaPlayer): Unit = {
      mp.stop()
      mp.reset()
      if (onPlayListener.isDefined) {
        onPlayListener.get.onComplete(current)
      }
      Log.i(tag, "MediaPlayer has completed playback of the sample: " + current.name)
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

  case class Sample(id: Long, name: String, df: FileDescriptor, pause: Int, first: Boolean = true) {}

  trait OnPlayListener {

    /**
     * Callback to be invoked when playback of a sample has started.
     *
     * @param sample the sample
     */
    def onStart(sample: Sample)

    /**
     * Callback to be invoked when playback of a sample has completed.
     *
     * @param sample the sample
     */
    def onComplete(sample: Sample)
  }

}


object MediaPlayerService {
  val ACTION_START = "mediaPlayerService.action.START"
  val ACTION_PAUSE = "mediaPlayerService.action.PAUSE"
  val ACTION_PLAY = "mediaPlayerService.action.PLAY"
  val ACTION_STOP = "mediaPlayerService.action.STOP"

  val EVENT_START_PLAYBACK = "mediaPlayerService.event.START_PLAYBACK"
  val EVENT_COMPLETE_PLAYBACK = "mediaPlayerService.event.COMPLETE_PLAYBACK"

  val MESSAGE_PLAY = 1
  val MESSAGE_PAUSE = 2
  val MESSAGE_CONTINUE = 3
  val MESSAGE_STOP = 4
}