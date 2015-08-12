package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.mesh.TriangleMesh
import scalismo.ui.visualization.props.HasScalarRange
import scalismo.ui.{ MeshView, ScalarMeshFieldView }

import scala.reflect.ClassTag
import scala.swing.{ Alignment, BorderPanel, GridPanel, Label }

class InformationPanel extends BorderPanel with PropertyPanel {
  override def description: String = "Information"

  val providers: List[InformationProvider] = List(MeshInformationProvider, MeshFieldInformationProvider, ScalarRangeInformationProvider)

  var target: Option[AnyRef] = None

  override def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    obj match {
      case Some(thing) =>
        val applicable = providers.filter(_.supports(thing))
        if (applicable.nonEmpty) {
          target = Some(thing)
          updateUi(applicable)
          true
        } else false
      case None => false
    }
  }

  def cleanup(): Unit = {
    peer.removeAll()
  }

  def updateUi(infos: List[InformationProvider]): Unit = {
    if (target.isDefined) {
      val panels = infos map createPanel
      panels.fold(this) { case (parent, child) => parent.layout(child) = BorderPanel.Position.North; child }
    }
  }

  def createPanel(info: InformationProvider): BorderPanel = new BorderPanel {

    def label(text: String): Label = new Label(text) {
      horizontalAlignment = Alignment.Left
    }

    val data = info.getInformation(target.get)
    val inner = new GridPanel(data.size, 2)
    inner.border = new TitledBorder(info.title)
    data.foreach {
      case (key, value) =>
        inner.contents ++= Seq(label(key), label(value))
    }
    layout(inner) = BorderPanel.Position.Center
  }
}

trait InformationProvider {
  def title: String

  def supports(obj: AnyRef): Boolean

  def getInformation(obj: AnyRef): Map[String, String]
}

abstract class TypedInformationProvider[T <: AnyRef: ClassTag] extends InformationProvider {

  import scala.language.implicitConversions

  private val clazz = implicitly[ClassTag[T]].runtimeClass

  final override def supports(obj: AnyRef): Boolean = {
    clazz.isInstance(obj)
  }

  final override def getInformation(obj: AnyRef): Map[String, String] = {
    val t = obj.asInstanceOf[T]
    about(t)
  }

  def about(t: T): Map[String, String]

  implicit def numberToString[V <: AnyVal](v: V): String = v.toString

}

object MeshInformationProvider extends TypedInformationProvider[MeshView] {
  override val title = "Mesh"

  override def about(t: MeshView): Map[String, String] = meshInfo(t.source)

  def meshInfo(t: TriangleMesh): Map[String, String] = {
    Map("Number of points" -> t.points.toSeq.length, "Number of cells" -> t.cells.length)
  }
}

object MeshFieldInformationProvider extends TypedInformationProvider[ScalarMeshFieldView] {
  override val title = "Mesh"

  override def about(t: ScalarMeshFieldView): Map[String, String] = MeshInformationProvider.meshInfo(t.source.mesh)
}

object ScalarRangeInformationProvider extends TypedInformationProvider[HasScalarRange] {

  override def title: String = "Scalar Values"

  override def about(t: HasScalarRange): Map[String, String] = {
    Map("Minimum" -> t.scalarRange.value.absoluteMinimum, "Maximum" -> t.scalarRange.value.absoluteMaximum)
  }
}
