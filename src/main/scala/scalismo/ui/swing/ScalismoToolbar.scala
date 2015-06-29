package scalismo.ui.swing

import scalismo.ui.Workspace

import scala.swing.Component

object ScalismoToolbar {
  val DefaultContentsFactory: Workspace => Seq[Component] = {
    ws: Workspace =>
      Seq(new ToggleLandmarkPickingButton(ws))
  }
}

class ScalismoToolbar(val workspace: Workspace) extends Toolbar {
  def initialContentsFactory: Workspace => Seq[Component] = ScalismoToolbar.DefaultContentsFactory

  floatable = false
  rollover = true
  initialContentsFactory(workspace).foreach({
    c => add(c)
  })
}