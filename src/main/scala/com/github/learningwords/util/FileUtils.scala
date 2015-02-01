package com.github.learningwords.util

import java.net.URISyntaxException

import android.content.Context
import android.database.Cursor
import android.net.Uri

object FileUtils {

  @throws[URISyntaxException]
  def getPath(context: Context, uri: Uri): String = {
    if ("content".equalsIgnoreCase(uri.getScheme)) {
      val projection = Array("_data")
      var cursor: Cursor = null

      try {
        cursor = context.getContentResolver.query(uri, projection, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow("_data")
        if (cursor.moveToFirst()) {
          return cursor.getString(column_index)
        }
      } catch {
        case e: Exception => e.printStackTrace()
        // Eat it
      }
    }
    else if ("file".equalsIgnoreCase(uri.getScheme)) {
      return uri.getPath
    }
    null
  }
}