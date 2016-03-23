package scalismo.ui.control.interactor.landmark.complex

import java.awt.event.MouseEvent

import scalismo.geometry._
import scalismo.mesh.TriangleMesh
import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.control.interactor.Recipe
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.model.capabilities.{InverseTransformation, Grouped}
import scalismo.ui.model.properties.Uncertainty
import scalismo.ui.model._
import scalismo.ui.view.{ViewportPanel3D, ViewportPanel2D, ViewportPanel}

object ReadyForCreating {
  def enter[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]]: StateTransition[InteractorType, DelegateType] = new StateTransition[InteractorType, DelegateType] {
    override def apply()(implicit parent: ComplexLandmarkingInteractor[InteractorType]): Delegate[InteractorType] = new ReadyForCreating[InteractorType]()
  }
}

class ReadyForCreating[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](implicit override val parent: ComplexLandmarkingInteractor[InteractorType]) extends ComplexLandmarkingInteractor.Delegate[InteractorType] {
  override def onLandmarkCreationToggled(): Unit = {
    if (!parent.isLandmarkCreationEnabled) {
      transitionToReadyForEditing()
    }
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    def exceptLandmarks(node: SceneNode) = node match {
      case nope: LandmarkNode => false
      case _ => true
    }
    Recipe.HighlightOutlineOfPickableObject.mouseMoved(e, exceptLandmarks)
  }

  override def mouseClicked(e: MouseEvent): Verdict = {
    getLandmarkForClick(e).foreach { case (lm, group) =>
        group.landmarks.add(lm.copy(id = group.landmarks.nameGenerator.nextName()))
    }
    Block
  }

  def transitionToReadyForEditing(): Unit = {
    parent.transitionTo(ReadyForEditing.enter)
  }

  def getLandmarkForClick(e: MouseEvent): Option[(Landmark[_3D], GroupNode)] = {
    val pointAndNode = e.viewport.rendererState.pointAndNodeAtPosition(e.getPoint)
    pointAndNode.nodeOption.flatMap { node =>
      val contextOption: Option[(Point3D, GroupNode)] = node match {
        case skip: LandmarkNode => None
        case ok: Grouped with InverseTransformation =>
          Some((ok.inverseTransform(pointAndNode.pointOption.get), ok.group))
        case ok: ImageNode =>
          // images don't support transformations
          Some((pointAndNode.pointOption.get, ok.group))
        case _ => None
      }

      contextOption.flatMap { case (point, group) =>
        val params = uncertaintyParametersFor(node, point, e.viewport)
        val axes: List[Vector3D] = params.map(_._1).getOrElse(Uncertainty.DefaultAxes)
        val sigmas = params.map(_._2).getOrElse(Uncertainty.DefaultSigmas)
        val uncertainty = Uncertainty(axes, sigmas)
        val landmark = new Landmark[_3D]("dummy", point, uncertainty = Some(uncertainty.to3DNormalDistribution))
        Some((landmark, group))
      }
    }
  }

  def uncertaintyParametersFor(node: SceneNode, point: Point3D, viewport: ViewportPanel): Option[(List[Vector3D], List[Float])] = {
    val meshOption: Option[TriangleMesh] = node match {
      case m: TriangleMeshNode => Some(m.source)
      case m: ScalarMeshFieldNode => Some(m.source.mesh)
    }

    meshOption.flatMap { mesh =>
      viewport match {
        case _2d: ViewportPanel2D =>
          val (planeNormal, meshNormal, meshTangential) = _2d.axis match {
            case Axis.Z =>
              val v1 = Vector3D(0, 0, 1)
              val v2: Vector3D = mesh.normalAtPoint(point).copy(z = 0)
              (v1, v2, Vector3D(-v2.y, v2.x, 0))
            case Axis.Y =>
              val v1 = Vector3D(0, 1, 0)
              val v2: Vector3D = mesh.normalAtPoint(point).copy(y = 0)
              (v1, v2, Vector3D(-v2.z, 0, v2.x))
            case Axis.X =>
              val v1 = Vector3D(1, 0, 0)
              val v2: Vector3D = mesh.normalAtPoint(point).copy(x = 0)
              (v1, v2, Vector3D(0, -v2.z, v2.y))
          }
          val axes = List(planeNormal, meshNormal, meshTangential).map { v => v * (1 / v.norm) : Vector3D }
          val sigmas = Uncertainty.DefaultSigmas
          Some((axes, sigmas))
        case _3d: ViewportPanel3D =>
          val meshNormal = mesh.normalAtPoint(point)
          val firstPerp = {
            /* There is an infinite number of perpendicular vectors, any one will do.
             * To find any perpendicular vector, just take the cross product with any other, non-parallel vector.
             * We try (1,0,0), and if it happened to be parallel, the crossproduct is (0,0,0), and we take another.
             */
            val candidate = meshNormal.crossproduct(Vector(1,0,0))
            if (candidate.norm2 != 0) candidate else meshNormal.crossproduct(Vector(0,1,0))
          }
          val secondPerp = meshNormal.crossproduct(firstPerp)
          val axes = List(meshNormal, firstPerp, secondPerp).map { v => v * (1 / v.norm) : Vector3D }
          val sigmas = Uncertainty.DefaultSigmas
          Some((axes, sigmas))
      }
    }
  }
}

