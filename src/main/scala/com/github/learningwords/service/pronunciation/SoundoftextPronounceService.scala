package com.github.learningwords.service.pronunciation

import java.io.InputStream

import com.github.learningwords.Language
import com.github.learningwords.exception.HttpClientException
import com.github.learningwords.http.RequestParameters

class SoundoftextPronounceService extends PronounceService {


  val baseUrl = "http://soundoftext.com/audio/{lang}/{word}.mp3"
  val load = "http://soundoftext.com/server-download.php"

  @throws[HttpClientException]
  override def getPronunciationAsStream(lang: Language, text: String): InputStream = {
    val response = httpTemplate.get(load, RequestParameters.create().addQueryParam("name", lang.name).addQueryParam("text", text).addQueryParam("id", lang.shortcut))
    if (response.getStatusLine.getStatusCode == 200) {
      return httpTemplate.getForStream(baseUrl, RequestParameters.create().addPathParam("lang", lang.name).addPathParam("word", text))
    }
    null
  }
}