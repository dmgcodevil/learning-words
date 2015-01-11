package com.github.learningwords.repository.util

import java.sql.SQLException

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.util.Log
import com.github.learningwords.domain.Language
import com.github.learningwords.repository.LanguageRepository
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils

/**
 * @author dmgcodevil
 */
class DatabaseHelper(_context: Context, dbName: String, cursorFactory: CursorFactory, version: Int)
  extends OrmLiteSqliteOpenHelper(_context, dbName, cursorFactory, version) {

  private val TAG: String = this.getClass.getSimpleName

  //the name of the database that will be stored into /data/data/APPNAME/DATABASE_NAME.db
  var DATABASE_NAME: String = null;
  // todo move it to property file

  //with each version increase the onUpgrade() method will be invoked;
  var DATABASE_VERSION: Int = 1

  //references to existing repositories
  private var languageRepository: LanguageRepository = null

  def this(c: Context) {
    this(c, "myappname.db", null, 1)
    DATABASE_NAME = "myappname.db"
    DATABASE_VERSION = 1
  }

  // Invoked only if a db file doesn't exist
  override def onCreate(db: SQLiteDatabase, connectionSource: ConnectionSource): Unit = {
    try {
      TableUtils.createTable(connectionSource, classOf[Language])
    }
    catch {
      case ex: SQLException =>
        Log.e(TAG, "error creating DB " + DATABASE_NAME)
        throw new RuntimeException(ex) // todo add and throw certain exception instead of general RuntimeException
    }
  }


  // invoked only if the version has been changed
  override def onUpgrade(db: SQLiteDatabase, connectionSource: ConnectionSource, oldVer: Int,
                         newVer: Int) {
    try {
      TableUtils.dropTable(connectionSource, classOf[Language], true) // todo apply concrete changes instead of deleting full table
      onCreate(db, connectionSource)
    }
    catch {
      case ex: SQLException =>
        Log.e(TAG, "error upgrading db " + DATABASE_NAME + "from ver " + oldVer)
        throw new RuntimeException(ex); // todo add and throw certain exception instead of general RuntimeException
    }
  }

  // Gets LanguageRepository instance,
  @throws[SQLException]
  def getLanguageRepository: LanguageRepository = {
    if (languageRepository == null) {
      languageRepository = new LanguageRepository(getConnectionSource, classOf[Language])
    }
    languageRepository
  }

  override def close(): Unit = {
    super.close()
    languageRepository = null
  }
}