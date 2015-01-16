package com.github.learningwords.service.pronunciation

import java.io.InputStream
import java.net.URL
import java.text.MessageFormat.format

class GooglePronounceService extends PronounceService {

  val baseUrl = "http://translate.google.com/translate_tts?tl={0}&q=\"{1}\""

  override def getPronunciationAsStream(lang: String, text: String): InputStream = {
    val url = new URL(format(baseUrl, lang, text))
    val httpcon = url.openConnection()
    httpcon.addRequestProperty("User-Agent", "anything")
    httpcon.getInputStream
  }
}