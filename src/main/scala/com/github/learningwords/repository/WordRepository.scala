package com.github.learningwords.repository

import java.sql.SQLException
import java.util.concurrent.Callable

import com.github.learningwords.domain.{Domain, Word, Translation}
import com.j256.ormlite.dao.{BaseDaoImpl, DaoManager, Dao}
import com.j256.ormlite.stmt.SelectArg
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.misc.TransactionManager
import collection.JavaConverters._

@throws[SQLException]
class WordRepository(connectionSource: ConnectionSource, dataClass: Class[Word])
  extends BaseDaoImpl[Word, Long](connectionSource, dataClass) with BaseRepository {

  val _connectionSource: ConnectionSource = connectionSource
  val _dataClass: Class[_ <: Domain] = dataClass

  @throws[SQLException]
  def addWord(word: Word) = {
    this.create(word)
    word
  }

  @throws[SQLException]
  def updateWord(word: Word) = {
    this.update(word)
    word
  }

  @throws[SQLException]
  def findWord(word: String, lang: String) = {
    val qb = this.queryBuilder()
    qb.where().eq(Word.LANG_FIELD_NAME, lang).and().eq(Word.WORD_FIELD_NAME, word)
    val words = this.query(qb.prepare())
    if(!words.isEmpty) Some(words.get(0)) else None
  }

  @throws[SQLException]
  def acquireWord(word: String, lang: String) = {
    val wordDao = this
    TransactionManager.callInTransaction(_connectionSource,
      new Callable[Word]() {
        override def call() = {
          val qb = wordDao.queryBuilder()
          qb.where().eq(Word.LANG_FIELD_NAME, lang).and().eq(Word.WORD_FIELD_NAME, word)
          val words = wordDao.query(qb.prepare())
          if(words.isEmpty) {
            val w = new Word(word, lang)
            wordDao.create(w)
            w
          }
          else {
            words.get(0)
          }
        }
      })
  }

  @throws[SQLException]
  def addTranslation(from: Long, to: Long) = {
    val wordDao = this
    TransactionManager.callInTransaction(_connectionSource,
      new Callable[Translation]() {
        override def call() = {
          val qb = translationDao.queryBuilder()
          qb.where().eq(Translation.FROM_ID_FIELD_NAME, from)
            .and().eq(Translation.TO_ID_FIELD_NAME, to)
          val translations = translationDao.query(qb.prepare())
          if(translations.isEmpty) {
            val translation = new Translation(wordDao.queryForId(from),
              wordDao.queryForId(to))
            translationDao.create(translation)
            translation
          }
          else {
            translations.get(0)
          }
        }
      })
  }

  @throws[SQLException]
  def findTranslations(word: Word) = {
    val translationQb = translationDao.queryBuilder()
    translationQb.selectColumns(Translation.TO_ID_FIELD_NAME)
    val userSelectArg = new SelectArg()
    translationQb.where().eq(Translation.FROM_ID_FIELD_NAME, userSelectArg)
    val wordQb = this.queryBuilder()
    val query = wordQb.where().in("id", translationQb).prepare()
    query.setArgumentHolderValue(0, word)
    this.query(query)
  }

  @throws[SQLException]
  def list(query: String) = {
    val rawResults = queryRaw("SELECT wordfrom." + Word.WORD_FIELD_NAME +
      ", wordto." + Word.WORD_FIELD_NAME +
      " FROM ((word wordfrom JOIN translation ON wordfrom.id = translation." +
      Translation.FROM_ID_FIELD_NAME + ") JOIN word wordto ON translation." +
      Translation.TO_ID_FIELD_NAME + " = wordto.id) WHERE wordfrom." +
      Word.WORD_FIELD_NAME + " LIKE ?", "%" + query + "%")
    val r = for(r <- rawResults.asScala) yield r(0) -> r(1)
    rawResults.close()
    r.toList
  }

  private val translationDao = new TranslationRepository(_connectionSource, classOf[Translation])
}

object WordRepository {
  def apply(connectionSource: ConnectionSource, dataClass: Class[Word]): WordRepository = {
    new WordRepository(connectionSource, dataClass)
  }
}
