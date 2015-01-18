package com.github.learningwords.service.pronunciation

import java.io.InputStream

import com.github.learningwords.Language
import com.github.learningwords.http.RequestParameters

class GooglePronounceService extends PronounceService {

  val googleBaseUrl = "http://translate.google.com/translate_tts"
  val baseUrl = "http://soundoftext.com/audio/Russian/{word}.mp3"

  override def getPronunciationAsStream(lang: Language, text: String): InputStream = {
    httpTemplate.getForStream(baseUrl, RequestParameters.create().addQueryParam("tl", lang.shortcut).addQueryParam("q", text))
  }
}