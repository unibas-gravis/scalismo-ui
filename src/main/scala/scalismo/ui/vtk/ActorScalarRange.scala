package scalismo.ui.vtk

import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.ScalarRangeProperty

trait ActorScalarRange extends SinglePolyDataActor {
  def scalarRange: ScalarRangeProperty

  protected def setScalarRange() = this.synchronized {
    mapper.SetScalarRange(scalarRange.value.cappedMinimum, scalarRange.value.cappedMaximum)
    mapper.Modified()
    publishEdt(VtkContext.RenderRequest(this))
  }

  listenTo(scalarRange)

  reactions += {
    case VisualizationProperty.ValueChanged(s) if s eq scalarRange => setScalarRange()
  }

  setScalarRange()

  override def onDestroy() = this.synchronized {
    deafTo(scalarRange)
    super.onDestroy()
  }

}
