package com.github.learningwords.service.pronunciation

import java.io.{IOException, InputStream}


class LocalStoragePronounceService extends PronounceService {
  @throws[IOException]
  override def getPronunciationAsStream(lang: String, text: String): InputStream = {
    null
  }
}
