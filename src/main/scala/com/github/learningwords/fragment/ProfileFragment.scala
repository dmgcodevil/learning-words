package com.github.learningwords.fragment

;


import android.app.Fragment
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{AdapterView, ArrayAdapter, ImageButton, Spinner}
import com.github.learningwords.domain.Profile
import com.github.learningwords.repository.ProfileRepository
import com.github.learningwords.repository.util.HelperFactory
import com.github.learningwords.util.LanguageReader
import com.github.learningwords.{Language, R}


/**
 * A simple {@link Fragment} subclass.
 */
class ProfileFragment extends Fragment {

  private var selectNativeLanguage: Spinner = null
  private var listItems: List[String] = List("")
  private var adapter: ArrayAdapter[String] = null
  private var nativeLanguage: String = null

  private var saveBtn: ImageButton = null

  private var profileRepository: ProfileRepository = null


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    profileRepository = HelperFactory.helper().getRepository(classOf[ProfileRepository])
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                            savedInstanceState: Bundle): View = {

    val view = inflater.inflate(R.layout.fragment_profile, container, false)
    selectNativeLanguage = view.findViewById(R.id.selectNativeLanguage).asInstanceOf[Spinner]
    saveBtn = view.findViewById(R.id.saveBtn).asInstanceOf[ImageButton]
    loadLanguages()
    registerListeners()

    view
  }

  private def registerListeners(): Unit = {
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

    saveBtn.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = {
        profileRepository.deleteAll() // todo make saveOrUpd
        val profile = new Profile(nativeLanguage)
        profileRepository.create(profile)
      }
    })

  }

  private def loadLanguages(): Unit = {
    var languages: List[Language] = LanguageReader.languages
    languages.foreach(l =>
      listItems = listItems.:+(l.name))
    val items = listItems.toArray
    adapter = new ArrayAdapter(getActivity, android.R.layout.simple_spinner_item, items)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    selectNativeLanguage.setAdapter(adapter)
    selectNativeLanguage.setSelection(0)
  }

  private def updateSaveBtn() {
    if (!Option(nativeLanguage).getOrElse("").isEmpty) saveBtn.setVisibility(View.VISIBLE) else saveBtn.setVisibility(View.INVISIBLE)
  }

}
