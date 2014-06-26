package org.statismo.stk.ui.swing.actions

import scala.swing.Action
import scala.swing.Dialog

import org.statismo.stk.ui.StatismoApp

class AboutAction(name: String) extends Action(name) {
  def apply() = {
    Dialog.showMessage(null,
      s"""
Statismo Viewer, Version ${org.statismo.stk.ui.Version}
	
Copyright 2014, University of Basel.
	      
Authors: 
 Ghazi Bouabene
 Christoph Langguth
 Marcel Lüthi

Feedback is very welcome!
christoph.langguth@unibas.ch""", title = name)
  }
}