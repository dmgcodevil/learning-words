package com.github.learningwords.service.pronunciation

import java.io.{IOException, InputStream}

import com.github.learningwords.Language


class LocalStoragePronounceService extends PronounceService {
  @throws[IOException]
  override def getPronunciationAsStream(lang: Language, text: String): InputStream = {
    null
  }
}
