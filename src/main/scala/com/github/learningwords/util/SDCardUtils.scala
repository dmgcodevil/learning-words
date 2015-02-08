package com.github.learningwords.util

import android.os.Environment

/**
 * @author dmgcodevil
 */
object SDCardUtils {

  /* Checks if external storage is available for read and write */
  def isExternalStorageWritable: Boolean = {
    val state = Environment.getExternalStorageState
    if (Environment.MEDIA_MOUNTED.equals(state)) true else false
  }

  /* Checks if external storage is available to at least read */
  def isExternalStorageReadable: Boolean = {
    val state = Environment.getExternalStorageState
    if (Environment.MEDIA_MOUNTED.equals(state) ||
      Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      return true
    }
    false
  }

}