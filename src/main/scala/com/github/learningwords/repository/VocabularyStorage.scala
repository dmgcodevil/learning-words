package com.github.learningwords.repository

class VocabularyStorage(fromLanguage: String, toLanguage: String) {
  def translate(word: String, categories: Option[List[String]]): Seq[String] = {
    translations.filter(_._1 == word).map(_._2)
  }

  def list(filter: String, categories: List[String] = List()): Seq[(String, String)] = {
    translations.filter(_._1.containsSlice(filter))
  }

  def add(word: String, translation: String) {
    translations += word -> translation
  }

  private class TranslationsSeq(storage: VocabularyStorage) extends Seq[(String, String)] {
    def apply(idx: Int) = storage.translations(idx)

    def length() = storage.translations.length

    def iterator() = storage.translations.iterator
  }

  val translations = collection.mutable.ArrayBuffer(
    "kill" -> "llik", "dead" -> "daed", "time" -> "emit")
}
