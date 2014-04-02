package org.statismo.stk.ui.settings

import java.io.File

object SettingsFile {

}

class StatismoSettingsFile extends SettingsFile(SettingsDirectory.get().get, "global.ini")

class SettingsFile(directory: File, name: String) {
  def getValues(key: String): Seq[String] = {
    //FIXME: returning home directory all the time, assuming that this is a "normal system"
    Seq(System.getProperty("user.home"))
  }
}
