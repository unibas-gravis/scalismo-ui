package scalismo.ui.control.interactor.landmark.complex.posterior

import breeze.linalg.{ DenseMatrix, DenseVector }
import scalismo.common.DiscreteVectorField
import scalismo.geometry._
import scalismo.statisticalmodel.{ MultivariateNormalDistribution, LowRankGaussianProcess, NDimensionalNormalDistribution }
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

  def inversePoseTransform: PointTransformation

  override protected def initialDelegate: Delegate[PosteriorLandmarkingInteractor] = {
    PosteriorReadyForCreating.enter()
  }

  def updatePreview(modelLm: LandmarkNode, targetLm: LandmarkNode, mousePosition: Point3D): Unit = {

    targetUncertaintyGroup.genericTransformations.foreach(_.remove())
    targetUncertaintyGroup.genericTransformations.add((p: Point[_3D]) => mousePosition, "mousePosition")

    val lmPointAndId = {
      previewNode.source.pointSet.findClosestPoint(modelLm.source.point)
    }

    val uncertaintyMean = DenseVector(0.0, 0.0, 0.0)
    val uncertaintyCovModelLm = modelLm.uncertainty.value.toMultivariateNormalDistribution.cov
    val uncertaintyCovTargetLm = targetLm.uncertainty.value.toMultivariateNormalDistribution.cov
    val lmUncertainty = MultivariateNormalDistribution(uncertaintyMean, uncertaintyCovModelLm + uncertaintyCovTargetLm)

    // Here, we need to (inverse) transform the mouse position in order to feed an non-rotated deformation vector to the regression
    val coeffs = sourceGpNode.transformation.gp.coefficients(IndexedSeq((lmPointAndId.point, inversePoseTransform(mousePosition) - lmPointAndId.point, lmUncertainty)))
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

