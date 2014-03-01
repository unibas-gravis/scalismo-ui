package org.statismo.stk.ui.swing

import scala.swing.Component

import org.statismo.stk.ui.Workspace

object StatismoToolbar {
  val DefaultContentsFactory: Workspace => Seq[Component] = {
    ws: Workspace =>
      Seq(new ToggleLandmarkPickingButton(ws))
  }
}

class StatismoToolbar(val workspace: Workspace) extends Toolbar {
  def initialContentsFactory: Workspace => Seq[Component] = StatismoToolbar.DefaultContentsFactory

  floatable = false
  rollover = true
  initialContentsFactory(workspace).foreach({
    c => add(c)
  })
}