package com.github.learningwords

import com.github.learningwords.PlaybackListener

class Playlist(val tracks: Seq[TrackDto]) {

  private var current = 0
  private var iterator: Iterator[TrackDto] = _
  private var listeners: List[PlaybackListener] = List()

  {
    iterator = tracks.iterator
  }




  def registerListener(listener: PlaybackListener): Unit = {
    listeners = listeners :+ listener
  }

  def next: Option[TrackDto] = {
    if (iterator.hasNext) {
      val track = iterator.next()
      current = iterator.indexOf(track)
      listeners.foreach(_.onNext(current, track))
      Some(track)
    }
    None
  }

  def hasNext = iterator.hasNext

  def switchTrack(track: TrackDto): Unit = {
    if (tracks.indexOf(track) != -1) {
      current = tracks.indexOf(track)
      iterator = slice.iterator
      listeners.foreach(_.onSwitch(current, track))
    }
  }

  def reset(): Unit = {
    iterator = tracks.iterator
    current = 0
    listeners.foreach(_.onReset())
  }

  def slice: Seq[TrackDto] = {
    tracks.drop(current)
  }

  def size = tracks.size

}