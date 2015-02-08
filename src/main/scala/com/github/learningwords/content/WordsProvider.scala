package com.github.learningwords.content

import android.content.{ContentProvider, ContentValues}
import android.net.Uri
import android.database.Cursor

class WordsProvider extends ContentProvider {
  def onCreate() = {
    true
  }

  override def query(uri: Uri, projection: Array[String],
    selection: String, selectionArgs: Array[String],
    sortOrder: String): Cursor = {
    null
  }

  override def insert(uri: Uri, values: ContentValues): Uri = {
    null
  }

  override def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    0
  }

  override def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    0
  }

  override def getType(uri: Uri): String = {
    ""
  }
}
