package org.statismo.stk.ui.view.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import scala.swing.Reactor
import org.statismo.stk.ui.Scene
import org.statismo.stk.ui.SceneChanged
import org.statismo.stk.ui.StatModel
import java.awt.Dimension
import scala.swing.Label
import scala.swing.Swing
import org.statismo.stk.ui.controller.PrincipalComponentsController


// This entire class is just a quick hack for now.
class DetailsPanel(val scene: Scene) extends BorderPanel with Reactor {
  listenTo(scene)
  reactions += {
    case SceneChanged() => onSceneChanged
  }
  
  //layout(new PrincipalComponentsPanel(None)) = Center
  
  def onSceneChanged = {
    val statModels = scene.objects.filter(_.isInstanceOf[StatModel]).map(_.asInstanceOf[StatModel])
    if (statModels.length < 1) {
      //peer.removeAll
    } else {
      val controller = new PrincipalComponentsController(statModels.head)
      //layout(new PrincipalComponentsPanel(controller)) = Center
    }
    revalidate
  }
}