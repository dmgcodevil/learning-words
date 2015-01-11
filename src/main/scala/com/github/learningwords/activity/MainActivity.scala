package com.github.learningwords.activity

import android.os.Bundle
import android.view.{Menu, MenuItem, View}
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.{AdapterView, ArrayAdapter, Spinner}
import com.github.learningwords.R
import com.github.learningwords.domain.Language
import com.github.learningwords.repository.LanguageRepository
import com.github.learningwords.repository.util.HelperFactory
import org.scaloid.common.SActivity


/**
 * @author dmgcodevil
 */
class MainActivity extends SActivity {

  private var languageRepository: LanguageRepository = null
  private var selectLanguageSpinner: Spinner = null
  private var listItems: List[String] = List[String]()
  private var adapter: ArrayAdapter[String] = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    selectLanguageSpinner = find[Spinner](R.id.selectLanguageSpinner)
    selectLanguageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      override def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
        selectLanguageSpinner.setSelection(position)
      }

      override def onNothingSelected(parent: AdapterView[_]): Unit = {}
    })
    HelperFactory.setHelper(getApplicationContext)
    languageRepository = HelperFactory.helper().getLanguageRepository
    initLanguages()
    loadLanguages()

  }


  private def initLanguages(): Unit = {
    languageRepository.deleteAll()
    languageRepository.create(new Language("English"))
    languageRepository.create(new Language("German"))
    languageRepository.create(new Language("Italian"))
  }

  private def loadLanguages(): Unit = {
    var languages: List[Language] = languageRepository.getAllLanguages
    languages.foreach(l =>
      listItems = listItems.+:(l.name))
    val items = listItems.toArray
    adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    selectLanguageSpinner.setAdapter(adapter)
  }


  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.main, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.getItemId
    if (id == R.id.action_settings) {
      return true
    }
    super.onOptionsItemSelected(item)
  }

  override def onDestroy(): Unit = {
    HelperFactory.releaseHelper()
    super.onDestroy()
  }
}
