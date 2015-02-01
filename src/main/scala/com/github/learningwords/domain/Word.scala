package com.github.learningwords.domain

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "word")
class Word extends Domain {
  @DatabaseField(columnName = Word.LANG_FIELD_NAME)
  var lang: String = _
  @DatabaseField(columnName = Word.WORD_FIELD_NAME)
  var word: String = _
  @DatabaseField(columnName = Word.PRONUNCIATION_URL_FIELD_NAME)
  var pronunciationUrl: String = _

  def this(word: String, lang: String, pronunciationUrl: String = "") {
    this()
    this.word = word
    this.lang = lang
    this.pronunciationUrl = pronunciationUrl
  }
}

object Word {
  final val LANG_FIELD_NAME = "lang"
  final val WORD_FIELD_NAME = "word"
  final val PRONUNCIATION_URL_FIELD_NAME = "pronunciation_url"
}
