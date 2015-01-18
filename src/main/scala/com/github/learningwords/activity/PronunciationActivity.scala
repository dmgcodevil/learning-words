package com.github.learningwords.activity

import android.app.Activity
import android.os.Bundle
import android.view.{Menu, MenuItem}
import android.widget.{LinearLayout, TextView}
import com.github.learningwords.{Word, Language, R}
import com.github.learningwords.fragment.PronounceFragment

class PronunciationActivity extends Activity {

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_pronunciation)



    val nativeWord = getIntent.getExtras.get("nativeWord").asInstanceOf[Word]
    val foreignWord = getIntent.getExtras.get("foreignWord").asInstanceOf[Word]

    val nativeWordTextView = findViewById(R.id.nativeWordTextView).asInstanceOf[TextView]
    val foreignWordTextView = findViewById(R.id.addPronunciation).asInstanceOf[TextView]

    val nativeFragContainer = findViewById(R.id.native_pronounce_Container).asInstanceOf[LinearLayout]
    getFragmentManager.beginTransaction().replace(nativeFragContainer.getId, PronounceFragment(nativeWord)).commit()

    val foreignFragContainer = findViewById(R.id.foreign_pronounce_Container).asInstanceOf[LinearLayout]
    getFragmentManager.beginTransaction().replace(foreignFragContainer.getId, PronounceFragment(foreignWord)).commit()


    nativeWordTextView.setText(nativeWord.value)
    foreignWordTextView.setText(foreignWord.value)
  }


  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.menu_pronunciation, menu)
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
