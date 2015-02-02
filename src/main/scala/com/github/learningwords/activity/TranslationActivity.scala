package com.github.learningwords.activity

import android.app.Activity
import android.os.Bundle
import android.view.{View, KeyEvent}
import android.widget.{Button, EditText, TextView, AutoCompleteTextView, ArrayAdapter}
import android.view.inputmethod.EditorInfo

import com.github.learningwords.{R, Language, WordDto}
import com.github.learningwords.repository.VocabularyStorage
import com.github.learningwords.service.transition.YandexTranslationService

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
    val wordEdit = findViewById(R.id.translation_word).asInstanceOf[EditText]
    wordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      override def onEditorAction(v: TextView, actionId: Int, event: KeyEvent) = {
        if(actionId == EditorInfo.IME_ACTION_DONE) {
          updateTranslations()
        }
        false
      }
    });
    wordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      override def onFocusChange(v: View, hasFocus: Boolean) {
        if(!hasFocus) {
          updateTranslations()
        }
      }
    });
  }

  private def updateTranslations() {
    val wordEdit = findViewById(R.id.translation_word).asInstanceOf[EditText]
    val translationEdit = findViewById(R.id.translation_translation).asInstanceOf[AutoCompleteTextView]
    val fromW = new WordDto(new Language(null, "en"), wordEdit.getText.toString)
    val toLang = new Language(null, "ru")
    val translations = YandexTranslationService.translate(fromW, toLang)
    translationEdit.setAdapter(new ArrayAdapter(this,
      android.R.layout.simple_dropdown_item_1line, translations.text))
    translationEdit.showDropDown()
  }

  private def addTranslation(word: String, translation: String) {
    storage.add(word, translation)
  }

  private val storage = new VocabularyStorage("en", "rus")
}
