package scalismo.ui.view.properties

import javax.swing.border.TitledBorder

import breeze.linalg.DenseVector
import scalismo.geometry._3D
import scalismo.registration.{ RigidTransformation, RigidTransformationSpace }
import scalismo.ui.model._
import scalismo.ui.view.ScalismoFrame

import scala.swing.GridBagPanel.Fill
import scala.swing._
import scala.util.{ Failure, Success, Try }

object RigidTransformationPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new RigidTransformationPropertyPanel(frame)
  }
}

class RigidTransformationPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Parameters"

  private var targets: List[TransformationNode[RigidTransformation[_3D]]] = Nil

  val textFields = Array.fill(6)(new TextField())
  val labels = List("T1", "T2", "T3", "R1", "R2", "R3")

  val panel = new GridBagPanel {
    val constraints = new Constraints()

    var (x, y) = (0, 0)

    def next = {
      constraints.gridx = x
      constraints.gridy = y

      constraints.weightx = x
      if (x == 1) {
        constraints.fill = Fill.Horizontal
      } else {
        constraints.fill = Fill.None
      }

      x += 1
      if (x == 2) {
        x = 0
        y += 1
      }
      constraints
    }

    labels.zip(textFields).foreach {
      case (l, f) =>
        add(new Label(l), next)
        add(f, next)
    }
  }

  val apply = new Button(new Action("Apply") {
    override def apply(): Unit = {

      def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
        val (ss: Seq[Success[T]] @unchecked, fs: Seq[Failure[T]] @unchecked) =
          xs.partition(_.isSuccess)

        if (fs.isEmpty) Success(ss map (_.get))
        else Failure[Seq[T]](fs.head.exception) // Only keep the first failure
      }

      val valuesTry = flatten(textFields.map(f => Try {
        java.lang.Float.parseFloat(f.text)
      }).toList)
      valuesTry match {
        case Success(values) =>
          val params = DenseVector(values.toArray)
          targets.foreach { node =>
            node.transformation = RigidTransformationSpace[_3D]().transformForParameters(params)
          }
        case _ =>
      }
    }
  })

  layout(new BorderPanel {
    layout(panel) = BorderPanel.Position.Center
    layout(apply) = BorderPanel.Position.South
    border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
  }) = BorderPanel.Position.North

  def updateUi() = {
    targets.headOption.foreach { node =>
      node.transformation.parameters.toArray.zip(textFields).foreach {
        case (f, t) =>
          t.text = f.toString
      }
    }
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    // we have to account for type erasure, that's why we need the collect
    val supported = allMatch[TransformationNode[_ <: PointTransformation]](nodes).collect { case tn if tn.transformation.isInstanceOf[RigidTransformation[_3D]] => tn.asInstanceOf[TransformationNode[RigidTransformation[_3D]]] }
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head)
      updateUi()
      true
    } else {
      false
    }
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t))
    targets = Nil
  }

  reactions += {
    case TransformationNode.event.TransformationChanged(_) => updateUi()
  }

}
