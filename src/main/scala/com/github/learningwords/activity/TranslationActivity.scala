package com.github.learningwords.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.{Button, EditText, AutoCompleteTextView}

import com.github.learningwords.R
import com.github.learningwords.repository.VocabularyStorage

// TODO: proper edit chaining, no multiline
class TranslationActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_translation)
    val doneButton = findViewById(R.id.translation_done).asInstanceOf[Button]
    doneButton.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View) {
        val wordEdit = findViewById(R.id.translation_word).asInstanceOf[EditText]
        val translationEdit = findViewById(R.id.translation_translation).asInstanceOf[EditText]
        addTranslation(wordEdit.getText.toString, translationEdit.getText.toString)
        finish()
      }
    })
  }

  private def addTranslation(word: String, translation: String) {
    storage.add(word, translation)
  }

  private val storage = new VocabularyStorage("en", "rus")
}
