package scalismo.ui.vtk

import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.RotationProperty

trait RotatableActor extends RenderableActor {
  def rotation: RotationProperty

  listenTo(rotation)

  reactions += {
    case VisualizationProperty.ValueChanged(s) => if (s eq rotation) onRotationChanged(rotation)
  }

  def onRotationChanged(rotation: RotationProperty): Unit

  override def onDestroy(): Unit = {
    deafTo(rotation)
    super.onDestroy()
  }
}
