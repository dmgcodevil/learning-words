package com.github.learningwords.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.{EditText, Button}

import com.github.learningwords.{Word, Language, R}


class MenuActivity extends Activity {


  private var nativeEdit: EditText = null;
  private var foreignEdit: EditText = null;

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_menu)
    val addPronunciation = findViewById(R.id.addPronunciation).asInstanceOf[Button]
    nativeEdit = findViewById(R.id.nativeEdit).asInstanceOf[EditText]
    foreignEdit = findViewById(R.id.foreignEdit).asInstanceOf[EditText]
    addPronunciation.setOnClickListener(new View.OnClickListener() {

      override def onClick(v: View): Unit = {
        val nativeWord = new Word(new Language("Russian", "ru"), nativeEdit.getText.toString.trim.toLowerCase)
        val foreignWord = new Word(new Language("English", "en"), foreignEdit.getText.toString.trim.toLowerCase)
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
