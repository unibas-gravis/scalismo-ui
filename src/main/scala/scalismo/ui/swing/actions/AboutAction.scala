package scalismo.ui.swing.actions

import scala.swing.{ Action, Dialog }

class AboutAction(name: String) extends Action(name) {
  /* If you are getting errors about the BuildInfo class in IntelliJ idea, edit
     the project settings to include target/scala-XXX/src_managed/main as a source folder.
   */
  def apply() = {
    Dialog.showMessage(null,
      s"""
Scalismo Viewer version: ${scalismo.ui.BuildInfo.version}
Scalismo version: ${scalismo.BuildInfo.version}
Scala version: ${scalismo.ui.BuildInfo.scalaVersion}

Copyright 2014-2015, University of Basel.

Authors: 
 Ghazi Bouabene
 Christoph Langguth
 Marcel Lüthi

Feedback is very welcome!""", title = name)
  }
}
