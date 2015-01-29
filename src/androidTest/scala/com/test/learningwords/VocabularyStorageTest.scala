package com.test.learningwords

import android.test.AndroidTestCase
import com.github.learningwords.repository.util.HelperFactory
import com.github.learningwords.repository.VocabularyStorage

class VocabularyStorageTest extends AndroidTestCase {
  import junit.framework.Assert._

  override def setUp() {
    HelperFactory.setHelper(mContext)
    storage = VocabularyStorage("en", "ru")
  }

  override def tearDown() {
    storage.clear()
    storage = null
  }

  def testInit() {
    val values = storage.list("")
    assertEquals(0, values.size)
  }

  def testAddSingle() {
    val word = "dead"
    val translation = "daed"
    storage.add(word, translation)
    val translations = storage.translate(word)
    assertEquals(List(translation), translations)
  }

  def testAddMultiple() {
    val pairs = Array("dead" -> "daed", "kill" -> "llik", "time" -> "emit")
    pairs.foreach((value: (String, String)) => storage.add(value._1, value._2))
    val actual = pairs.map((value: (String, String)) => storage.translate(value._1))
    val expected = pairs.map((value: (String, String)) => (List(value._2):Seq[String]))
    for((l, r) <- expected zip actual) {
      assertEquals(l, r)
    }
  }

  def testListSingle() {
    val word = "dead"
    val translation = "daed"
    storage.add(word, translation)
    val actual = storage.list("")
    assertEquals(1, actual.size)
    assertEquals(word -> translation, actual(0))
  }

  def testListMultiple() {
    val pairs = Array("dead" -> "daed", "kill" -> "llik", "time" -> "emit")
    pairs.foreach((value: (String, String)) => storage.add(value._1, value._2))
    val actual = storage.list("")
    assertEquals(pairs.toSet, actual.toSet)
  }

  def testListFilterQuery() {
    val pairs = Array("dead" -> "daed", "kill" -> "llik", "time" -> "emit")
    pairs.foreach((value: (String, String)) => storage.add(value._1, value._2))
    val actual = storage.list("d")
    assertEquals(Set("dead" -> "daed"), actual.toSet)
  }

  def testAddDuplicate() {
    val word = "dead"
    val translation = "daed"
    storage.add(word, translation)
    storage.add(word, translation)
    val actual = storage.list("")
    assertEquals(List(word -> translation), actual)
  }

  private var storage: VocabularyStorage = null
}
