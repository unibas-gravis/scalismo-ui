package scalismo.ui.control.interactor.landmark.complex.posterior

import breeze.linalg.{ DenseMatrix, DenseVector }
import scalismo.common.DiscreteVectorField
import scalismo.geometry._
import scalismo.statisticalmodel.{ LowRankGaussianProcess, NDimensionalNormalDistribution }
import scalismo.statisticalmodel.LowRankGaussianProcess.Eigenpair
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.Delegate
import scalismo.ui.model._

trait PosteriorLandmarkingInteractor extends ComplexLandmarkingInteractor[PosteriorLandmarkingInteractor] {

  implicit val theFrame = frame

  def previewNode: TriangleMeshNode

  def sourceGpNode: TransformationNode[DiscreteLowRankGpPointTransformation]

  def previewGpNode: TransformationNode[DiscreteLowRankGpPointTransformation]

  def targetUncertaintyGroup: GroupNode

  def targetGroupNode: GroupNode

  override protected def initialDelegate: Delegate[PosteriorLandmarkingInteractor] = {
    PosteriorReadyForCreating.enter()
  }


  def updatePreview(modelLm: LandmarkNode, targetLm: LandmarkNode, mousePosition: Point3D): Unit = {

    targetUncertaintyGroup.transformations.foreach(_.remove())
    targetUncertaintyGroup.transformations.add((p: Point[_3D]) => mousePosition, "mousePosition")

    val lmPointAndId = {
      previewNode.source.pointSet.findClosestPoint(modelLm.source.point)
    }

    val error = NDimensionalNormalDistribution(scalismo.geometry.Vector(0f, 0f, 0f), modelLm.uncertainty.value.to3DNormalDistribution.cov + targetLm.uncertainty.value.to3DNormalDistribution.cov)

    val coeffs = sourceGpNode.transformation.gp.coefficients(IndexedSeq((lmPointAndId.point, mousePosition - lmPointAndId.point, error)))
    previewGpNode.transformation = sourceGpNode.transformation.copy(coeffs)
  }

  def showPreview(): Unit = {
    previewNode.visible = true
  }

  def hidePreview(): Unit = {
    previewNode.visible = false
  }

  def initialize(): Unit = {
    previewNode.pickable.value = false
    hidePreview()
  }
}

