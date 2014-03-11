package org.statismo.stk.ui.swing

import scala.swing.ToggleButton
import scala.swing.event.ButtonClicked

import org.statismo.stk.ui.Workspace

class ToggleLandmarkPickingButton(val workspace: Workspace) extends ToggleButton("LM") {
  selected = workspace.landmarkClickMode
  reactions += {
    case ButtonClicked(s) =>
      workspace.landmarkClickMode = peer.isSelected
  }
}