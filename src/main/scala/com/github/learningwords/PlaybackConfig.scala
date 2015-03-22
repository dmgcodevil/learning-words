package com.github.learningwords


/**
 * Playback configuration.
 *
 * @param shortDelay the delay between two words in a track
 * @param longDelay the delay between two track in a playlist
 */
class PlaybackConfig(val shortDelay: Long = 0L, val longDelay: Long = 0L) extends Serializable{
}
