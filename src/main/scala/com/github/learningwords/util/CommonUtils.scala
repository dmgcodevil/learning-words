package com.github.learningwords.util

import android.content.Context

/**
 * @author dmgcodevil
 */
object CommonUtils {

  def getApplicationName(context: Context): String = {
    val stringId = context.getApplicationInfo.labelRes
    context.getString(stringId)
  }

  /**
   * Retrieves name of the app from package name that specified in AndroidManifest.xml,
   * for example: if package="com.github.learningwords" then method returns "learningwords"
   * @param context
   * @return
   */
  def getShortAppName(context: Context): String = {
    // todo improve this method
    val m = context.getPackageManager
    var packageName = context.getPackageName
    packageName.split("\\.") match {
      case Array(com, domain, name) => name
    }
  }
}