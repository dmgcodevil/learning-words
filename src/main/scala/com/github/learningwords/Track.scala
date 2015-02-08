package com.github.learningwords

class Track(var id: Long, var native: WordDto, var foreign: WordDto, order: Long = 0L) extends Serializable{

}