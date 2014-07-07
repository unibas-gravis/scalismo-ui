package org.statismo.stk.ui.swing

import scala.swing.ToggleButton
import scala.swing.event.ButtonClicked

import org.statismo.stk.ui.Workspace

class ToggleLandmarkPickingButton(val workspace: Workspace) extends ToggleButton("LM") {
  selected = workspace.landmarkClickMode
  updateTooltip()

  reactions += {
    case ButtonClicked(s) =>
      workspace.landmarkClickMode = peer.isSelected
      updateTooltip()
  }

  def updateTooltip() = {
    tooltip = "Landmark Clicking (" + {
      if (peer.isSelected) "on" else "off"
    } + ")"
  }
}