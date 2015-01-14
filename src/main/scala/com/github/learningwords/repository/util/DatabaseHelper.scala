package com.github.learningwords.repository.util

import java.sql.SQLException

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.util.Log
import com.github.learningwords.Language
import com.github.learningwords.repository.util.cfg.{Configuration, DBConfig}
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils

import scala.collection.mutable

/**
 * @author dmgcodevil
 */
class DatabaseHelper(_context: Context, dbName: String, cursorFactory: CursorFactory, version: Int)
  extends OrmLiteSqliteOpenHelper(_context, dbName, cursorFactory, version) {

  val TAG: String = this.getClass.getSimpleName

  val entityClasses = Configuration.entities
  val repositories = mutable.Map[Class[_ <: Dao[_, _]], Any]()

  //references to existing repositories
  // private var languageRepository: LanguageRepository = null

  def this(c: Context) {
    this(c, DBConfig.dbName, null, DBConfig.dbVersion)
    initRepositories()
  }

  def initRepositories(): Unit = {
    Log.i(TAG, "initializing repositories...")
    //repositories.put(classOf[LanguageRepository], new LanguageRepository(getConnectionSource, classOf[Language]))
    Log.i(TAG, "repositories have bean initialized")
  }

  // Invoked only if a db file doesn't exist
  override def onCreate(db: SQLiteDatabase, connectionSource: ConnectionSource): Unit = {
    try {

      entityClasses.foreach(eClass => TableUtils.createTable(connectionSource, eClass))
    }
    catch {
      case ex: SQLException =>
        Log.e(TAG, "error creating DB " + DBConfig.dbName)
        throw new RuntimeException(ex) // todo add and throw certain exception instead of general RuntimeException
    }
  }

  // with each db version increase the onUpgrade() method will be invoked
  override def onUpgrade(db: SQLiteDatabase, connectionSource: ConnectionSource, oldVer: Int,
                         newVer: Int) {
    try {
      TableUtils.dropTable(connectionSource, classOf[Language], true) // todo apply concrete changes instead of deleting full table
      onCreate(db, connectionSource)
    }
    catch {
      case ex: SQLException =>
        Log.e(TAG, "error upgrading db " + DBConfig.dbName + "from ver " + oldVer)
        throw new RuntimeException(ex); // todo add and throw certain exception instead of general RuntimeException
    }
  }

  def getRepository[T <: Dao[_, _]](repositoryClass: Class[T]): T = {
    if (repositories.contains(repositoryClass)) {
      repositories(repositoryClass).asInstanceOf[T]
    } else {
      throw new RuntimeException("repository with type: '" + repositoryClass + "' doesn't exist")
    }
  }

  override def close(): Unit = {
    super.close()
    repositories.clear()
  }

}