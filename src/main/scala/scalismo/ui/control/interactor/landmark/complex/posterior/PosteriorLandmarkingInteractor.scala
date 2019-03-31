/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.control.interactor.landmark.complex.posterior

import breeze.linalg.DenseVector
import scalismo.geometry._
import scalismo.statisticalmodel.MultivariateNormalDistribution
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.Delegate
import scalismo.ui.model._

trait PosteriorLandmarkingInteractor extends ComplexLandmarkingInteractor[PosteriorLandmarkingInteractor] {

  implicit val theFrame = frame
  private lazy val nodeVisibility = frame.sceneControl.nodeVisibility

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
    nodeVisibility.setVisibility(previewNode, frame.perspective.viewports, show = true)
  }

  def hidePreview(): Unit = {
    nodeVisibility.setVisibility(previewNode, frame.perspective.viewports, show = false)
  }

  def initialize(): Unit = {
    previewNode.pickable.value = false
    hidePreview()
  }
}

