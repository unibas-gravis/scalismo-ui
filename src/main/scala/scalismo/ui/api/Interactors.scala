package scalismo.ui.api

import scalismo.ui.control.interactor.DefaultInteractor
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor
import scalismo.ui.control.interactor.landmark.complex.posterior.PosteriorLandmarkingInteractor
import scalismo.ui.model.{TriangleMeshNode, GroupNode, DiscreteLowRankGpPointTransformation, TransformationNode}
import scalismo.ui.view.ScalismoFrame

class SimplePosteriorLandmarkingInteractor(ui : ScalismoUI, modelView : StatisticalMeshModelView, targetGroup : Group) extends PosteriorLandmarkingInteractor {



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


class LandmarkingInteractor(ui : ScalismoUI) extends DefaultInteractor with ComplexLandmarkingInteractor[LandmarkingInteractor] {
  override def frame: ScalismoFrame = ui.frame
}

