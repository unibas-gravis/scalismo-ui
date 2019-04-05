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

package scalismo.ui.control.interactor.landmark.complex

import java.awt.event.MouseEvent
import java.awt.{ Color, Cursor }

import scalismo.geometry._
import scalismo.mesh.TriangleMesh
import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.control.interactor.{ DefaultInteractor, DelegatedInteractor, DelegatingInteractor }
import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.model._
import scalismo.ui.model.capabilities.{ Grouped, InverseTransformation }
import scalismo.ui.model.properties.Uncertainty
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel, ViewportPanel2D, ViewportPanel3D }

import scala.swing.ToggleButton
import scala.swing.event.ButtonClicked

object ComplexLandmarkingInteractor {

  trait Delegate[InteractorType <: ComplexLandmarkingInteractor[InteractorType]] extends DelegatedInteractor[ComplexLandmarkingInteractor[InteractorType]] {
    def onLandmarkCreationToggled()

  }

  trait StateTransition[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]] {
    def apply()(implicit parent: InteractorType): Delegate[InteractorType]
  }

  class Instance(override val frame: ScalismoFrame) extends DefaultInteractor with ComplexLandmarkingInteractor[Instance] {

  }

}

trait ComplexLandmarkingInteractor[InteractorType <: ComplexLandmarkingInteractor[InteractorType]] extends DelegatingInteractor[ComplexLandmarkingInteractor[InteractorType]] with ScalismoPublisher {

  implicit lazy val myself: InteractorType = this.asInstanceOf[InteractorType]

  private lazy val landmarkingButton: ToggleButton = new ToggleButton {
    selected = true
    private val myIcon = BundledIcon.Landmark

    def updateUi(): Unit = {
      val onOff = if (selected) "ON" else "OFF"
      tooltip = s"Toggle landmarking (currently $onOff)"
      val iconColor = if (selected) Color.GREEN.darker else Color.DARK_GRAY
      icon = myIcon.colored(iconColor).standardSized()
    }

    reactions += {
      case ButtonClicked(_) => updateUi()
    }

    updateUi()
  }

  override protected def initialDelegate: Delegate[InteractorType] = {
    if (isLandmarkCreationEnabled) {
      ReadyForCreating.enter()
    } else {
      ReadyForEditing.enter()
    }
  }

  override def onActivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.add(landmarkingButton)
  }

  override def onDeactivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.remove(landmarkingButton)
  }

  // set the cursor to a crosshair if we're in landmarking mode
  override def mouseEntered(e: MouseEvent): Verdict = {
    val cursor = if (landmarkingButton.selected) Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) else Cursor.getDefaultCursor
    e.canvas.setCursor(cursor)
    super.mouseEntered(e)
  }

  def transitionTo(transition: StateTransition[InteractorType, _ <: Delegate[InteractorType]]): Unit = {
    delegate = transition.apply()
  }

  def getLandmarkForClick(e: MouseEvent): Option[(Landmark[_3D], GroupNode)] = {
    val pointAndNode = e.viewport.rendererState.pointAndNodeAtPosition(e.getPoint)
    pointAndNode.nodeOption.flatMap { node =>
      val contextOption: Option[(Point3D, GroupNode)] = node match {
        case _: LandmarkNode => None
        case ok: Grouped with InverseTransformation =>
          Some((ok.inverseTransform(pointAndNode.pointOption.get), ok.group))
        case ok: ImageNode =>
          // images don't support transformations
          Some((pointAndNode.pointOption.get, ok.group))
        case _ => None
      }

      contextOption.flatMap {
        case (point, group) =>
          val params = uncertaintyParametersFor(node, group, point, e.viewport)
          val axes: List[EuclideanVector3D] = params.map(_._1).getOrElse(Uncertainty.DefaultAxes)
          val sigmas = params.map(_._2).getOrElse(sigmasForLandmarkUncertainty(group))
          val uncertainty = Uncertainty(axes, sigmas)
          val landmark = new Landmark[_3D]("dummy", point, uncertainty = Some(uncertainty.toMultivariateNormalDistribution))
          Some((landmark, group))
        case _ => None
      }
    }
  }

  def uncertaintyParametersFor(node: SceneNode, group: GroupNode, point: Point3D, viewport: ViewportPanel): Option[(List[EuclideanVector3D], List[Double])] = {
    val meshOption: Option[TriangleMesh[_3D]] = node match {
      case m: TriangleMeshNode => Some(m.source)
      case m: ScalarMeshFieldNode => Some(m.source.mesh)
      case _ => None
    }

    meshOption.flatMap { mesh =>
      viewport match {
        case _2d: ViewportPanel2D =>
          val (planeNormal, meshNormal, meshTangential) = _2d.axis match {
            case Axis.Z =>
              val v1 = EuclideanVector3D(0, 0, 1)
              val v2: EuclideanVector3D = mesh.vertexNormals(mesh.pointSet.findClosestPoint(point).id).copy(z = 0)
              (v1, v2, EuclideanVector3D(-v2.y, v2.x, 0))
            case Axis.Y =>
              val v1 = EuclideanVector3D(0, 1, 0)
              val v2: EuclideanVector3D = mesh.vertexNormals(mesh.pointSet.findClosestPoint(point).id).copy(y = 0)
              (v1, v2, EuclideanVector3D(-v2.z, 0, v2.x))
            case Axis.X =>
              val v1 = EuclideanVector3D(1, 0, 0)
              val v2: EuclideanVector3D = mesh.vertexNormals(mesh.pointSet.findClosestPoint(point).id).copy(x = 0)
              (v1, v2, EuclideanVector3D(0, -v2.z, v2.y))
          }
          val axes = List(planeNormal, meshNormal, meshTangential).map { v => v * (1 / v.norm): EuclideanVector3D }
          val sigmas = sigmasForLandmarkUncertainty(group)
          Some((axes, sigmas))
        case _: ViewportPanel3D =>
          val meshNormal = mesh.vertexNormals(mesh.pointSet.findClosestPoint(point).id)
          val firstPerp = {
            /* There is an infinite number of perpendicular vectors, any one will do.
             * To find any perpendicular vector, just take the cross product with any other, non-parallel vector.
             * We try (1,0,0), and if it happened to be parallel, the crossproduct is (0,0,0), and we take another.
             */
            val candidate = meshNormal.crossproduct(EuclideanVector3D(1, 0, 0))
            if (candidate.norm2 != 0) candidate else meshNormal.crossproduct(EuclideanVector3D(0, 1, 0))
          }
          val secondPerp = meshNormal.crossproduct(firstPerp)
          val axes = List(meshNormal, firstPerp, secondPerp).map { v => v * (1 / v.norm): EuclideanVector3D }
          val sigmas = sigmasForLandmarkUncertainty(group)
          Some((axes, sigmas))
      }
    }
  }

  def sigmasForLandmarkUncertainty(group: GroupNode): List[Double] = {
    Uncertainty.DefaultSigmas
  }

  listenTo(landmarkingButton)

  def isLandmarkCreationEnabled: Boolean = landmarkingButton.selected

  reactions += {
    case ButtonClicked(`landmarkingButton`) =>
      val del = delegate: Delegate[InteractorType]
      del.onLandmarkCreationToggled()
  }
}
