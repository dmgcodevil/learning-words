package com.github.learningwords.activity

import android.app.Activity
import android.os.{Environment, StrictMode, Bundle}
import android.view.{Menu, MenuItem, View}
import android.widget._
import com.github.learningwords.domain.Profile
import com.github.learningwords.repository.ProfileRepository
import com.github.learningwords.repository.util.HelperFactory
import com.github.learningwords.service.MediaService
import com.github.learningwords.service.pronunciation.{PronounceService, PronounceServiceType}
import com.github.learningwords.util.LanguageReader
import com.github.learningwords.{Language, R}


/**
 * @author dmgcodevil
 */
class MainActivity extends Activity {


  private var selectNativeLanguage: Spinner = null
  private var selectLearningLanguage: Spinner = null
  private var listItems: List[String] = List("")
  private var adapter: ArrayAdapter[String] = null
  private var nativeLanguage: String = null
  private var learningLanguage: String = null
  private var saveBtn: ImageButton = null
  private var loadPronounceButton: Button = null
  private val pronounceService = PronounceService(PronounceServiceType.Google)
  private var mediaService: MediaService = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    HelperFactory.setHelper(getApplicationContext)
    mediaService = new MediaService(getApplicationContext)
    val profileRepository = HelperFactory.helper().getRepository(classOf[ProfileRepository])
    selectNativeLanguage = findViewById(R.id.selectNativeLanguage).asInstanceOf[Spinner]
    selectLearningLanguage = findViewById(R.id.selectLearningLanguage).asInstanceOf[Spinner]
    saveBtn = findViewById(R.id.saveBtn).asInstanceOf[ImageButton]
    loadPronounceButton = findViewById(R.id.loadPronounceButton).asInstanceOf[Button]
    saveBtn.setVisibility(View.INVISIBLE)

    val policy = new StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)


    saveBtn.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = {
        val profile = new Profile(nativeLanguage)
        profileRepository.create(profile)
      }
    })
    loadPronounceButton.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = {
        val is = pronounceService.getPronunciationAsStream("en", "hello")
        val fileName: String = "hello.mp3"
        mediaService.save(fileName, is)
        Toast.makeText(getApplicationContext, "saved to " + Environment.getExternalStorageDirectory + "/pronunciation/" + fileName, Toast.LENGTH_LONG).show()
      }
    })

    if (profileRepository.getProfile != null) {
      Toast.makeText(getApplicationContext, "profile created", Toast.LENGTH_LONG).show()
    }

    def updateSaveBtn() {
      if (!Option(learningLanguage).getOrElse("").isEmpty && !Option(nativeLanguage).getOrElse("").isEmpty) {
        saveBtn.setVisibility(View.VISIBLE)
      } else {
        saveBtn.setVisibility(View.INVISIBLE)
      }
    }

    selectNativeLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener {
      override def onNothingSelected(parent: AdapterView[_]): Unit = {
        nativeLanguage = ""
        updateSaveBtn()
      }

      override def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
        nativeLanguage = selectNativeLanguage.getSelectedItem.asInstanceOf[String]
        updateSaveBtn()
      }
    })

    selectLearningLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener {
      override def onNothingSelected(parent: AdapterView[_]): Unit = {
        learningLanguage = ""
        updateSaveBtn()
      }

      override def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
        learningLanguage = selectLearningLanguage.getSelectedItem.asInstanceOf[String]
        updateSaveBtn()
      }
    })

    loadLanguages()
  }

  private def loadLanguages(): Unit = {
    LanguageReader(getApplicationContext)
    var languages: List[Language] = LanguageReader.languages
    languages.foreach(l =>
      listItems = listItems.:+(l.name))
    val items = listItems.toArray
    adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    selectNativeLanguage.setAdapter(adapter)
    selectLearningLanguage.setAdapter(adapter)
    selectNativeLanguage.setSelection(0)
    selectLearningLanguage.setSelection(0)
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
