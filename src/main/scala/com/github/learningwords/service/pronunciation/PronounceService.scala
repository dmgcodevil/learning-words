package com.github.learningwords.service.pronunciation

import java.io.{IOException, InputStream}

import com.github.learningwords.service.pronunciation.PronounceServiceType.PronounceServiceType

trait PronounceService {
  @throws[IOException]
  def getPronunciationAsStream(lang: String, text: String): InputStream
}

object PronounceService {
  def apply(pType: PronounceServiceType) = pType match {
    case PronounceServiceType.Google => new GooglePronounceService
    case PronounceServiceType.LocalStorage => new LocalStoragePronounceService
  }
}