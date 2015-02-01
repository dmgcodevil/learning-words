package com.github.learningwords.service.transition

import com.github.learningwords.http.RequestParameters
import com.github.learningwords.{Language, TranslationDto, WordDto}

object YandexTranslationService extends TranslationService {

  private val key = "trnsl.1.1.20150201T154249Z.f8e0a0478967f48b.cc1e0d141ea9b6f80d3a8e4ef24797dbcb27aaf7"
  val baseUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate"

  override def translate(word: WordDto, toLang: Language): TranslationDto = {
    val lang = word.lang.shortcut + "-" + toLang.shortcut
    val text = word.value
    httpTemplate.getForObject(baseUrl,
      RequestParameters.create().addQueryParam("key", key)
        .addQueryParam("lang", lang)
        .addQueryParam("text", text),
      classOf[TranslationDto])
  }
}