package com.github.learningwords.repository

import com.github.learningwords.domain.Word
import com.github.learningwords.repository.util.HelperFactory
import collection.JavaConversions._

class VocabularyStorage(fromLanguage: String, toLanguage: String) {
  def translate(word: String, categories: Option[List[String]] = None): Seq[String] = {
    wordRepo.findWord(word, fromLanguage) match {
      case Some(word) => wordRepo.findTranslations(word).toList.map(_.word)
      case None => List()
    }
  }

  def list(filter: String, categories: List[String] = List()): Seq[(String, String)] = {
    wordRepo.list(filter)
  }

  def add(word: String, translation: String) {
    val fromWord = wordRepo.acquireWord(word, fromLanguage)
    val toWord = wordRepo.acquireWord(translation, toLanguage)
    wordRepo.addTranslation(fromWord.id, toWord.id)
  }

  def clear() {
    wordRepo.deleteAll()
    translationRepo.deleteAll()
  }

  private val wordRepo = HelperFactory.helper().getRepository(classOf[WordRepository])
  private val translationRepo = HelperFactory.helper().getRepository(classOf[TranslationRepository])
}

object VocabularyStorage {
  def apply(fromLanguage: String, toLanguage: String) =
    new VocabularyStorage(fromLanguage, toLanguage)
}
