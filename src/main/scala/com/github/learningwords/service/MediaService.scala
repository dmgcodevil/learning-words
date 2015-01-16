package com.github.learningwords.service

import java.io.{FileOutputStream, File, IOException, InputStream}

import android.content.Context
import android.os.Environment
import android.provider.MediaStore.Files
import org.apache.commons.io.IOUtils

class MediaService(val context: Context) {

  @throws[IOException]
  def save(filename: String, is: InputStream): Unit = {

    val folder = new File(Environment.getExternalStorageDirectory + "/pronunciation");
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

}