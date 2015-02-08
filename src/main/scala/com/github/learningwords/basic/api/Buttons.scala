package com.github.learningwords.basic.api

import android.view.View
import android.widget.Button

object Buttons {

  def setOnClick(button: Button, f: () => Unit): Button = {
    button.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = f.apply()
    })
    button
  }
}