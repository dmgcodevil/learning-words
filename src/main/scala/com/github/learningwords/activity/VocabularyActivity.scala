package com.github.learningwords.activity

import android.app.ListActivity
import android.view.{Menu, MenuItem, MenuInflater}
import android.database.Cursor
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.SimpleCursorAdapter
import android.os.Bundle
import android.database.AbstractCursor

import com.github.learningwords.R
import com.github.learningwords.repository.VocabularyStorage

class VocabularyActivity extends ListActivity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    adapter = new SimpleCursorAdapter(this, R.layout.vocabulary_item,
      null, Array("_id", "translation"),
      Array(R.id.vocabulary_item_word, R.id.vocabulary_item_translation),
      0)
    setContentView(R.layout.activity_vocabulary)
    val searchView = findViewById(R.id.vocabulary_search).asInstanceOf[SearchView]
    setListAdapter(adapter)
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener {
      def onQueryTextChange(newText: String) = {
        false
      }
      def onQueryTextSubmit(query: String) = {
        setQuery(query)
        false
      }
    })
    searchView.setOnCloseListener(new SearchView.OnCloseListener {
      def onClose(): Boolean = {
        setQuery("")
        false
      }
    })
    setQuery("")
  }

  override def onDestroy() {
    super.onDestroy()
    setListAdapter(null)
    adapter = null
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    val inflater = getMenuInflater()
    inflater.inflate(R.menu.menu_vocabulary_options, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    val res = item.getItemId match {
      case R.id.action_add_translation => addTranslation; true
      case _ => super.onOptionsItemSelected(item)
    }
    res
  }

  private def setQuery(query: String) {
    adapter.swapCursor(new SeqCursor(storage.list(query)))
  }

  private def addTranslation() {
  }

  private class SeqCursor(seq: Seq[(String, String)]) extends AbstractCursor {
    def getColumnNames() = {
      Array("_id", "translation")
    }
    def getCount() = {
      seq.length
    }
    def getString(column: Int) = {
      val (first, second) = seq(getPosition)
      if(column == 0)
        first
      else if(column == 1)
        second
      else
        null
    }

    override def getType(columnIndex: Int) = Cursor.FIELD_TYPE_STRING
    def isNull(column: Int) = column >= 2
    def getDouble(column: Int):Double = {
      0.0
    }
    def getFloat(column: Int):Float = {
      0.0f
    }
    def getInt(column: Int):Int = {
      0
    }
    def getLong(column: Int):Long = {
      0
    }
    def getShort(column: Int):Short = {
      0
    }
  }

  private val storage = new VocabularyStorage("en", "rus")
  private var adapter: SimpleCursorAdapter = null
}
