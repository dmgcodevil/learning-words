package com.github.learningwords.util

import android.content.Context
import com.github.learningwords.Language

import scala.io.Source


object LanguageReader {

  private var langs: List[Language] = List()

  def languages = langs

  def apply(context: Context): Unit = {
    val inputStream = context.getAssets.open("langs")
    for (line <- Source.fromInputStream(inputStream).getLines()) {
      val elements = line.split(",")
      langs = langs :+ new Language(elements(0), elements(1))
    }
  }

}