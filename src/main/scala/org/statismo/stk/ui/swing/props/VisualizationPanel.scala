package org.statismo.stk.ui.swing.props

import org.statismo.stk.ui.visualization.{Visualization, VisualizationProvider}
import org.statismo.stk.ui.SceneTreeObject
import scala.collection.immutable

trait VisualizationsPropertyPanel extends PropertyPanel {
  def setVisualizations(visualizations: immutable.Seq[Visualization[_]]): Boolean
  override def setObject(obj: Option[AnyRef]): Boolean = {
    throw new UnsupportedOperationException
  }
}

class VisualizationPanel(description: String, delegates: VisualizationsPropertyPanel*) extends CombinedPropertiesPanel(description, delegates:_*) {
  override def setObject(obj: Option[AnyRef]): Boolean = {
    obj match {
      case None => false
      case Some(vp: SceneTreeObject with VisualizationProvider[_]) =>
        val vis: immutable.Seq[Visualization[_]] = {
          val tries = vp.scene.viewports.map {context =>
            vp.scene.visualizations.tryGet(vp, context)
          }
          tries.filter(_.isSuccess).map(_.get).toList
        }
        if (vis.isEmpty) false
        else {
          val ok = delegates.map(d => delegatedSetVisualizations(d, vis)).foldLeft(false)({
            (x, y) => x || y
          })
          revalidate()
          ok
        }
      case _ => false
    }
  }

  def delegatedSetVisualizations(del: VisualizationsPropertyPanel, visualizations: immutable.Seq[Visualization[_]]): Boolean = {
    val ok = del.setVisualizations(visualizations)
    del.visible = ok
    ok
  }

}
