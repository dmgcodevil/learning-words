package com.github.learningwords.service

import java.io.{FileOutputStream, File, IOException, InputStream}

import android.content.Context
import android.os.Environment
import android.provider.MediaStore.Files
import com.github.learningwords.{Word, Language}
import org.apache.commons.io.IOUtils

class MediaService(val context: Context) {

  @throws[IOException]
  @deprecated
  def save(filename: String, is: InputStream): Unit = {

    val folder = new File(Environment.getExternalStorageDirectory + "/pronunciation")
    val success = true
    if (!folder.exists()) {
      val success = folder.mkdir()
    }
    val fullFileName = folder.getPath + "/" + filename
    if (success) {
      val outputStream = new FileOutputStream(new File(fullFileName))
      IOUtils.copy(is, outputStream)
      outputStream.flush()
      outputStream.close()
    } else {
      // Do something else on failure
    }
  }

  def save(word: Word, is: InputStream): String = {

    val folder = new File(Environment.getExternalStorageDirectory + "/pronunciation/" + word.lang.shortcut)
    val success = true
    if (!folder.exists()) {
      val success = folder.mkdir()
    }
    val fullFileName = folder.getPath + "/" + word.value + ".mp3"
    if (success) {
      val outputStream = new FileOutputStream(new File(fullFileName))
      IOUtils.copy(is, outputStream)
      outputStream.flush()
      outputStream.close()
      fullFileName
    } else {
      null
    }
  }


  def exists(word: Word): Boolean = {
    val file = new File(buildFilePath(word))
    file.exists() && !file.isDirectory
  }

  def buildFilePath(word: Word): String = {
    Environment.getExternalStorageDirectory + "/pronunciation/" + word.lang.shortcut + "/" + word.value + ".mp3"
  }

}