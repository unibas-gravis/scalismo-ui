package scalismo.ui.swing.actions

import scala.swing.{ Action, Dialog }

class AboutAction(name: String) extends Action(name) {
  def apply() = {
    Dialog.showMessage(null,
      s"""
Scalismo Viewer, Version ${scalismo.ui.Version}

Copyright 2014-2015, University of Basel.

Authors: 
 Ghazi Bouabene
 Christoph Langguth
 Marcel Lüthi

Feedback is very welcome!""", title = name)
  }
}
