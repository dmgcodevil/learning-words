package com.github.learningwords.repository.util

import android.content.Context

import com.j256.ormlite.android.apptools.{OrmLiteSqliteOpenHelper, OpenHelperManager}

/**
 * @author dmgcodevil
 */
object HelperFactory {

  var databaseHelper: DatabaseHelper = null

  def setHelper(context: Context): Unit = {
    databaseHelper = OpenHelperManager.getHelper(context, classOf[DatabaseHelper])
  }

  def helper(): DatabaseHelper = {
    databaseHelper
  }

  def releaseHelper(): Unit = {
    OpenHelperManager.releaseHelper()
    databaseHelper = null
  }
}