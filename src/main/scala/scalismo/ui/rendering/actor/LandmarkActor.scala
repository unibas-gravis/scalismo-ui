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

package scalismo.ui.rendering.actor

import scalismo.geometry.{ SquareMatrix, _3D }
import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties._
import scalismo.ui.model.{ BoundingBox, LandmarkNode }
import scalismo.ui.rendering.actor.mixin.{ ActorColor, ActorLineWidth, ActorOpacity, ActorSceneNode }
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import vtk._

object LandmarkActor extends SimpleActorsFactory[LandmarkNode] {
  override def actorsFor(renderable: LandmarkNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new LandmarkActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new LandmarkActor2D(renderable, _2d))
    }
  }
}

trait LandmarkActor extends ActorColor with ActorOpacity with ActorSceneNode {
  override def sceneNode: LandmarkNode

  override lazy val color: ColorProperty = sceneNode.color
  override lazy val opacity: OpacityProperty = sceneNode.opacity

  private val ellipsoid = new vtkParametricEllipsoid
  private val functionSource = new vtkParametricFunctionSource
  protected val transformFilter = new vtkTransformFilter()
  private val transform = new vtkTransform

  // wire everything together
  functionSource.SetParametricFunction(ellipsoid)
  transformFilter.SetInputConnection(functionSource.GetOutputPort())
  transformFilter.SetTransform(transform)

  //FIXME: pick control -- this should probably go into a trait or something.
  sceneNode match {
    case p: HasPickable =>
      SetPickable(if (p.pickable.value) 1 else 0)
      listenTo(p.pickable)
      reactions += {
        case NodeProperty.event.PropertyChanged(s) if s == p.pickable =>
          SetPickable(if (p.pickable.value) 1 else 0)
      }
    case _ =>
  }

  protected def rerender(geometryChanged: Boolean) = {
    if (geometryChanged) {
      val (xRadius, yRadius, zRadius) = {
        val r = sceneNode.uncertainty.value.sigmas.toArray
        (r(0), r(1), r(2))
      }

      transform.Identity()
      transform.PostMultiply()

      val m: SquareMatrix[_3D] = sceneNode.uncertainty.value.rotationMatrix
      val matrix = new vtkMatrix4x4
      // vtk uses 4x4 "homogeneous coordinates", so zero out the last row and column...
      matrix.Zero()
      //... , set the very last element to 1....
      matrix.SetElement(3, 3, 1)
      // ... and fill the rest with the actual data.
      for (r <- 0 to 2; c <- 0 to 2) {
        matrix.SetElement(r, c, m(r, c))
      }
      transform.SetMatrix(matrix)

      val center = sceneNode.transformedSource.point
      transform.Translate(center(0), center(1), center(2))

      ellipsoid.SetXRadius(xRadius)
      ellipsoid.SetYRadius(yRadius)
      ellipsoid.SetZRadius(zRadius)
    }

    actorChanged(geometryChanged)
  }

  listenTo(sceneNode, sceneNode.uncertainty)

  reactions += {
    case Transformable.event.GeometryChanged(_) => rerender(true)
    case NodeProperty.event.PropertyChanged(p) if p == sceneNode.uncertainty => rerender(true)
  }

  protected def onInstantiated(): Unit

  onInstantiated()

  rerender(true)
}

class LandmarkActor2D(override val sceneNode: LandmarkNode, viewport: ViewportPanel2D) extends SlicingActor(viewport) with LandmarkActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = sceneNode.lineWidth

  override protected def onSlicingPositionChanged(): Unit = rerender(false)

  override protected def onInstantiated(): Unit = {
    planeCutter.SetInputConnection(transformFilter.GetOutputPort())
  }

  override protected def sourceBoundingBox: BoundingBox = {
    transformFilter.Update()
    VtkUtil.bounds2BoundingBox(transformFilter.GetOutput().GetBounds())
  }

}

class LandmarkActor3D(override val sceneNode: LandmarkNode) extends SinglePolyDataActor with LandmarkActor {
  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(transformFilter.GetOutputPort())
  }
}

