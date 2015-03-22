package com.github.learningwords

trait PlaybackListener {

  def onNext(pos: Int, track: TrackDto)
  def onSwitch(pos: Int, track: TrackDto)
  def onReset()
}