package com.github.learningwords.service

import java.io.{IOException, File, FileOutputStream, InputStream}

import android.content.Context
import android.os.Environment
import com.github.learningwords.Word
import com.github.learningwords.exception.StorageUnavailableException
import com.github.learningwords.util.{CommonUtils, SDCardUtils}
import org.apache.commons.io.IOUtils

/**
 * @author dmgcodevil
 */
class MediaService(val context: Context) {

  @throws[StorageUnavailableException]
  @throws[IOException]
  def save(word: Word, is: InputStream): String = {
    MediaService.createLangDir(word)
    val fullFileName = getFilePath(word)
    val outputStream = new FileOutputStream(new File(fullFileName))
    IOUtils.copy(is, outputStream)
    outputStream.flush()
    IOUtils.closeQuietly(outputStream)
    fullFileName
  }


  def exists(word: Word): Boolean = {
    val file = new File(getFilePath(word))
    file.exists() && !file.isDirectory
  }


  def getFilePath(w: Word): String = {
    val appName = MediaService.appName
    val sdcard = Environment.getExternalStorageDirectory
    val lang = w.lang.shortcut
    val word = w.value
    s"$sdcard/$appName/pronunciation/$lang/$word.mp3"
  }

  def getPronunciationRecordFolder = MediaService.rootPronunciationFolder + MediaService.pronunciationRecordFolder

}

object MediaService {
  // sort app name is taken from the app package name
  private var appName: String = null
  private var pronunciationFolder = "/pronunciation"
  private var pronunciationRecordFolder = "/record"
  private var rootPronunciationFolder: String = null

  def apply(context: Context): MediaService = {
    if (!SDCardUtils.isExternalStorageWritable) {
      throw StorageUnavailableException.create("SD card is unavailable for write operations")
    }
    appName = CommonUtils.getShortAppName(context)
    rootPronunciationFolder = Environment.getExternalStorageDirectory + "/" + appName + pronunciationFolder
    createFolder(rootPronunciationFolder, "failed to create folder for pronunciations")
    createFolder(rootPronunciationFolder + pronunciationRecordFolder, "failed to create folder for pronunciations records")
    new MediaService(context)
  }

  private def createLangDir(word: Word) {
    val lang = word.lang.shortcut
    createFolder(rootPronunciationFolder + "/" + lang, s"failed to create folder for '$lang' pronunciations")
  }

  private def createFolder(path: String, msg: String = "failed to create folder"): Unit = {
    val folder = new File(path)
    if (!folder.exists()) {
      if (!folder.mkdirs()) throw new RuntimeException(msg)
    }
  }
}