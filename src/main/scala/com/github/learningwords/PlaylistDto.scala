package com.github.learningwords

class PlaylistDto(val tracks: List[Track]) extends Serializable {

  var shortDelay = 0L
  var longDelay = 0L

}