package scalismo.ui.api

import scalismo.ui.control.interactor.{DefaultInteractor, Interactor}
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor
import scalismo.ui.control.interactor.landmark.complex.posterior.PosteriorLandmarkingInteractor
import scalismo.ui.model.{TriangleMeshNode, GroupNode, DiscreteLowRankGpPointTransformation, TransformationNode}
import scalismo.ui.view.ScalismoFrame

trait SimpleInteractor {
  type ConcreteInteractor <: Interactor
  protected[api] def peer : ConcreteInteractor
}

case class SimplePosteriorLandmarkingInteractor(ui : ScalismoUI, modelView : StatisticalMeshModelView, targetGroup : Group) extends SimpleInteractor{

  type ConcreteInteractor = PosteriorLandmarkingInteractor

  override protected[api] val peer = new PosteriorLandmarkingInteractor {


    private val previewGroup = Group(ui.frame.scene.groups.add("__preview__", ghost = true))


    override val previewNode: TriangleMeshNode = ui.show(modelView.meshView.triangleMesh, "previewMesh", previewGroup).peer
    previewNode.visible = false
    previewNode.pickable.value = false

    override def sourceGpNode: TransformationNode[DiscreteLowRankGpPointTransformation] = modelView.shapeTransformationView.peer

    override def targetGroupNode: GroupNode = targetGroup.peer

    override val previewGpNode: TransformationNode[DiscreteLowRankGpPointTransformation] = {
      ui.show(modelView.shapeTransformationView.discreteLowRankGaussianProcess, "preview-gp", previewGroup).peer
    }

    override def frame: ScalismoFrame = ui.frame
  }
}


case class SimpleLandmarkingInteractor(ui : ScalismoUI) extends SimpleInteractor {


  override type ConcreteInteractor = Instance

  private[api] class Instance(override val frame: ScalismoFrame) extends DefaultInteractor with ComplexLandmarkingInteractor[Instance] {}

  override protected[api] val peer: Instance = new Instance(ui.frame)
}


