package scalismo.ui.api

import java.awt.Color
import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor
import scalismo.ui.control.interactor.landmark.complex.posterior.PosteriorLandmarkingInteractor
import scalismo.ui.control.interactor.landmark.simple.SimpleLandmarkingInteractorTrait
import scalismo.ui.control.interactor.{ Recipe, DefaultInteractor, Interactor }
import scalismo.ui.model._
import scalismo.ui.model.properties.Uncertainty
import scalismo.ui.view.ScalismoFrame
import scalismo.geometry._

private[api] sealed trait SimpleInteractor {
  type ConcreteInteractor <: Interactor
  val ui: ScalismoUI

  protected[api] def peer: ConcreteInteractor

  ui.frame.interactor = peer
  peer.onActivated(ui.frame)

}

case class SimplePosteriorLandmarkingInteractor(ui: ScalismoUI, modelGroup: Group, targetGroup: Group) extends SimpleInteractor {

  type ConcreteInteractor = PosteriorLandmarkingInteractor

  override protected[api] lazy val peer = new PosteriorLandmarkingInteractor {

    val meshView = ui.find[TriangleMeshView](modelGroup, (p: TriangleMeshView) => true).get
    val shapeTransformationView = ui.find[DiscreteLowRankGPTransformationView](modelGroup, (p: DiscreteLowRankGPTransformationView) => true).get

    private val previewGroup = Group(ui.frame.scene.groups.add("__preview__", ghost = true))

    // we start by copying all transformations of the modelGroup into the previewGroup. The order is important
    modelGroup.peer.genericTransformations.reverse.foreach { transNode =>
      previewGroup.peer.genericTransformations.add(transNode.transformation.asInstanceOf[PointTransformation], transNode.name)
    }

    override val previewNode: TriangleMeshNode = ui.show(previewGroup, meshView.triangleMesh, "previewMesh").peer
    previewNode.visible = false
    previewNode.color.value = Color.YELLOW
    previewNode.pickable.value = false

    override val targetUncertaintyGroup = Group(ui.frame.scene.groups.add("__target_preview__", ghost = true)).peer

    override def sourceGpNode: TransformationNode[DiscreteLowRankGpPointTransformation] =
      ui.find[DiscreteLowRankGPTransformationView](modelGroup, (p: DiscreteLowRankGPTransformationView) => true).get.peer

    override def targetGroupNode: GroupNode = targetGroup.peer

    override val previewGpNode: TransformationNode[DiscreteLowRankGpPointTransformation] = {
      ui.find[DiscreteLowRankGPTransformationView](previewGroup, (tv: DiscreteLowRankGPTransformationView) => true).get.peer
    }

    override def frame: ScalismoFrame = ui.frame

    override val inversePoseTransform = ui.filter[RigidTransformationView](modelGroup, (rv: RigidTransformationView) => true).reverse.foldLeft((p: Point[_3D]) => p) {
      case (a, b) =>
        (p: Point[_3D]) => a(b.transformation.inverse(p))
    }

  }
}

case class SimpleLandmarkingInteractor(ui: ScalismoUI) extends SimpleInteractor {

  override type ConcreteInteractor = Instance

  private[api] class Instance(override val frame: ScalismoFrame) extends DefaultInteractor with ComplexLandmarkingInteractor[Instance] {}

  override protected[api] lazy val peer: Instance = new Instance(ui.frame)
}

/**
 * This landmarking interactor does not edit uncertainties of landmarks.
 */
case class OneClickLandmarkingInteractor(ui: ScalismoUI, uncertainty: Uncertainty = Uncertainty.DefaultUncertainty) extends SimpleInteractor {

  override type ConcreteInteractor = Instance

  private[api] class Instance() extends SimpleLandmarkingInteractorTrait {

    override val defaultUncertainty = uncertainty

    override def mousePressed(e: MouseEvent): Verdict = Recipe.Block2DRotation.mousePressed(e)
  }

  override protected[api] lazy val peer: Instance = new Instance()
}

