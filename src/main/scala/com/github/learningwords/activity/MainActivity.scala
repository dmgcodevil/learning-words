package com.github.learningwords.activity

import android.app.Activity
import android.content.Intent
import android.os.{Bundle, StrictMode}
import android.view.{Menu, MenuItem}
import android.widget._
import com.github.learningwords.R
import com.github.learningwords.fragment.ProfileFragment
import com.github.learningwords.repository.ProfileRepository
import com.github.learningwords.repository.util.HelperFactory
import com.github.learningwords.util.LanguageReader


/**
 * @author dmgcodevil
 */
class MainActivity extends Activity {


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    HelperFactory.setHelper(getApplicationContext)
    LanguageReader(getApplicationContext)
    val profileRepository = HelperFactory.helper().getRepository(classOf[ProfileRepository])

    val policy = new StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)




    if (profileRepository.getProfile != null) {
      Toast.makeText(getApplicationContext, "profile created", Toast.LENGTH_LONG).show()
      val intent = new Intent(getApplicationContext, classOf[MenuActivity])
      startActivity(intent)
    } else {
      val container = findViewById(R.id.container).asInstanceOf[LinearLayout]
      getFragmentManager.beginTransaction().add(container.getId, new ProfileFragment()).commit()
    }


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
