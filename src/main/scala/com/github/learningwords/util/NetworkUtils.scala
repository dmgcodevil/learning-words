package com.github.learningwords.util

import android.net.ConnectivityManager

object NetworkUtils {
  def isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean = {
    val activeNetworkInfo = connectivityManager.getActiveNetworkInfo
    activeNetworkInfo != null && activeNetworkInfo.isConnected
  }
}