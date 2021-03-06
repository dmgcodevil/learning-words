package com.github.learningwords.activity

import android.app.{FragmentManager, Activity}
import android.os.Bundle
import android.view.{Menu, MenuItem}
import android.widget.{LinearLayout, TextView}
import com.github.learningwords.{WordDto, Language, R}
import com.github.learningwords.fragment.PronounceFragment

class PronunciationActivity extends Activity {

  private val PRONOUNCE_FRAGMENT_TAG = "pronounceFragment"

  val PRONOUNCE_FRAGMENT_NATIVE_TAG = PRONOUNCE_FRAGMENT_TAG + "_native"
  val PRONOUNCE_FRAGMENT_FOREIGN_TAG = PRONOUNCE_FRAGMENT_TAG + "_foreign"

  private var pronounceNativeFragment: PronounceFragment = null
  private var pronounceForeignFragment: PronounceFragment = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_pronunciation)

    val fm: FragmentManager = getFragmentManager

    val nativeWord = getIntent.getExtras.get("nativeWord").asInstanceOf[WordDto]
    val foreignWord = getIntent.getExtras.get("foreignWord").asInstanceOf[WordDto]

    val nativeWordTextView = findViewById(R.id.nativeWordTextView).asInstanceOf[TextView]
    val foreignWordTextView = findViewById(R.id.addPronunciation).asInstanceOf[TextView]

    pronounceNativeFragment = fm.findFragmentByTag(PRONOUNCE_FRAGMENT_NATIVE_TAG).asInstanceOf[PronounceFragment]
    pronounceForeignFragment = fm.findFragmentByTag(PRONOUNCE_FRAGMENT_FOREIGN_TAG).asInstanceOf[PronounceFragment]



    // If the Fragment is non-null, then it is currently being
    // retained across a configuration change.


    if (pronounceNativeFragment == null) {
      pronounceNativeFragment = PronounceFragment(nativeWord, "native")
      val container: LinearLayout = findViewById(R.id.native_pronounce_Container).asInstanceOf[LinearLayout]
      fm.beginTransaction.add(pronounceNativeFragment, PRONOUNCE_FRAGMENT_NATIVE_TAG).commit
      fm.beginTransaction.replace(container.getId, pronounceNativeFragment).commit
    }



    if (pronounceForeignFragment == null) {
      pronounceForeignFragment = PronounceFragment(foreignWord, "foreign")
      val container: LinearLayout = findViewById(R.id.foreign_pronounce_Container).asInstanceOf[LinearLayout]
      fm.beginTransaction.add(pronounceForeignFragment, PRONOUNCE_FRAGMENT_FOREIGN_TAG).commit
      fm.beginTransaction.replace(container.getId, pronounceForeignFragment).commit
    }


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
