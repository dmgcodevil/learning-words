package com.github.learningwords

class TrackDto(var id: Long, var native: WordDto, var foreign: WordDto, order: Long = 0L) extends Serializable{

}