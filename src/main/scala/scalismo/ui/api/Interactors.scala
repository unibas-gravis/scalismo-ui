package scalismo.ui.api

import java.awt.Color
import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor
import scalismo.ui.control.interactor.landmark.complex.posterior.PosteriorLandmarkingInteractor
import scalismo.ui.control.interactor.landmark.simple.SimpleLandmarkingInteractorTrait
import scalismo.ui.control.interactor.{ DefaultInteractor, Interactor, Recipe }
import scalismo.ui.model._
import scalismo.ui.model.properties.Uncertainty
import scalismo.ui.view.ScalismoFrame
import scalismo.geometry._
import scalismo.registration.RigidTransformationSpace

private[api] sealed trait SimpleInteractor {
  type ConcreteInteractor <: Interactor
  val ui: ScalismoUI

  protected[api] def peer: ConcreteInteractor

  ui.frame.interactor = peer
  peer.onActivated(ui.frame)

}

case class SimplePosteriorLandmarkingInteractor(ui: ScalismoUI, modelGroup: Group, targetGroup: Group) extends SimpleInteractor {

  type ConcreteInteractor = PosteriorLandmarkingInteractor

  private val nodeVisibility = ui.frame.sceneControl.nodeVisibility
  override protected[api] lazy val peer = new PosteriorLandmarkingInteractor {

    val meshView = ui.find[TriangleMeshView](modelGroup, (p: TriangleMeshView) => true).get
    //  val shapeTransformationView = ui.find[DiscreteLowRankGPTransformationView](modelGroup, (p: DiscreteLowRankGPTransformationView) => true).get

    private val previewGroup = Group(ui.frame.scene.groups.add("__preview__", ghost = true), ui.frame)

    // we start by copying the shape model transformations of the modelGroup into the previewGroup
    modelGroup.peer.shapeModelTransformations.poseTransformation.map(p => previewGroup.peer.shapeModelTransformations.addPoseTransformation(p.transformation, "pose"))
    modelGroup.peer.shapeModelTransformations.gaussianProcessTransformation.map(g => previewGroup.peer.shapeModelTransformations.addGaussianProcessTransformation(g.transformation, "shape"))

    override val previewNode: TriangleMeshNode = ui.show(previewGroup, meshView.triangleMesh, "previewMesh").peer
    nodeVisibility.setVisibility(previewNode, frame.perspective.viewports, false)
    previewNode.color.value = Color.YELLOW
    previewNode.pickable.value = false

    override val targetUncertaintyGroup = Group(ui.frame.scene.groups.add("__target_preview__", ghost = true), ui.frame).peer

    override def sourceGpNode: TransformationNode[DiscreteLowRankGpPointTransformation] = modelGroup.peer.shapeModelTransformations.gaussianProcessTransformation.get

    override def targetGroupNode: GroupNode = targetGroup.peer

    override val previewGpNode: TransformationNode[DiscreteLowRankGpPointTransformation] = previewGroup.peer.shapeModelTransformations.gaussianProcessTransformation.get

    override def frame: ScalismoFrame = ui.frame

    override val inversePoseTransform = modelGroup.peer.shapeModelTransformations.poseTransformation.map(_.transformation.inverse).getOrElse(PointTransformation.RigidIdentity)

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

