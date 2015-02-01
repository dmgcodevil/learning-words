package com.github.learningwords.service.transition

import com.github.learningwords.{TranslationDto, Language, WordDto}
import com.github.learningwords.http.HttpTemplate

trait TranslationService {

  val httpTemplate = new HttpTemplate()

  def translate(word: WordDto, toLang: Language):TranslationDto
}