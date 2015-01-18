package com.github.learningwords.service.pronunciation

import java.io.{IOException, InputStream}

import com.github.learningwords.Language
import com.github.learningwords.http.HttpTemplate
import com.github.learningwords.service.pronunciation.PronounceServiceType.PronounceServiceType

trait PronounceService {
  val httpTemplate = new HttpTemplate()

  @throws[IOException]
  def getPronunciationAsStream(lang: Language, text: String): InputStream
}

object PronounceService {
  def apply(pType: PronounceServiceType) = pType match {
    case PronounceServiceType.Google => new GooglePronounceService
    case PronounceServiceType.LocalStorage => new LocalStoragePronounceService
    case PronounceServiceType.Soundoftext => new SoundoftextPronounceService
  }
}