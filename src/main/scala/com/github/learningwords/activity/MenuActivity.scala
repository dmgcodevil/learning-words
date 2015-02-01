package com.github.learningwords.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.{Toast, EditText, Button}
import com.github.learningwords.service.transition.YandexTranslationService

import com.github.learningwords.{WordDto, Language, R}


class MenuActivity extends Activity {


  private var nativeEdit: EditText = null
  private var foreignEdit: EditText = null
  private var translateBtn: Button = null
  private var textToTranslate: EditText = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_menu)
    val addPronunciation = findViewById(R.id.addPronunciation).asInstanceOf[Button]
    nativeEdit = findViewById(R.id.nativeEdit).asInstanceOf[EditText]
    foreignEdit = findViewById(R.id.foreignEdit).asInstanceOf[EditText]
    textToTranslate = findViewById(R.id.textToTranslate).asInstanceOf[EditText]
    translateBtn = findViewById(R.id.translateBtn).asInstanceOf[Button]
    translateBtn.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View): Unit = {
        val translate = YandexTranslationService
        val fromW = new WordDto(new Language(null, "ru"), textToTranslate.getText.toString)
        val toLang = new Language(null, "en")
        try {
          val translationDto = translate.translate(fromW, toLang)
          Toast.makeText(MenuActivity.this, translationDto.text(0).toString, Toast.LENGTH_SHORT).show()
        } catch {
          case e: Throwable => e.printStackTrace()
        }
      }
    })

    addPronunciation.setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View): Unit = {
        val nativeWord = new WordDto(new Language("Russian", "ru"), nativeEdit.getText.toString.trim.toLowerCase)
        val foreignWord = new WordDto(new Language("English", "en"), foreignEdit.getText.toString.trim.toLowerCase)
        val intent = new Intent(getApplicationContext, classOf[PronunciationActivity])
        intent.putExtra("nativeWord", nativeWord)
        intent.putExtra("foreignWord", foreignWord)
        startActivity(intent)
      }
    })
  }


  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.menu_menu, menu)
    true
  }


  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.getItemId

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true
    }

    super.onOptionsItemSelected(item)
  }
}
