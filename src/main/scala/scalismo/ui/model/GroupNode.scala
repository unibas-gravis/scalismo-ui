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

package scalismo.ui.model

import scalismo.registration.{ RigidTransformation, RigidTransformationSpace }
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.model.Scene.event.SceneChanged
import scalismo.ui.model.capabilities.{ Removeable, Renameable }

class GroupsNode(override val parent: Scene) extends SceneNodeCollection[GroupNode] {
  override val name = "Groups"

  def add(name: String, ghost: Boolean = false): GroupNode = {
    val node = new GroupNode(this, name, ghost)
    add(node)
    node
  }

  // the groups node is always collapsed in the view.
  override def isViewCollapsed: Boolean = true
}

class GroupNode(override val parent: GroupsNode, initialName: String, private var _isGhost: Boolean) extends SceneNode with Renameable with Removeable with ScalismoPublisher {
  name = initialName

  def isGhost_=(b: Boolean): Unit = {
    _isGhost = b
    scene.publishEvent(SceneChanged(scene))
  }
  def isGhost = _isGhost

  val genericTransformations = new GenericTransformationsNode(this)
  val shapeModelTransformations = new ShapeModelTransformationsNode(this)

  val landmarks = new LandmarksNode(this)
  val triangleMeshes = new TriangleMeshesNode(this)
  val colorMeshes = new ColorMeshesNode(this)
  val scalarMeshFields = new ScalarMeshFieldsNode(this)
  val lineMeshes = new LineMeshesNode(this)
  val vectorFields = new VectorFieldsNode(this)
  val pointClouds = new PointCloudsNode(this)
  val images = new ImagesNode(this)
  val scalarFields = new ScalarFieldsNode(this)

  override val children: List[SceneNode] = List(
    genericTransformations,
    shapeModelTransformations,
    landmarks,
    triangleMeshes,
    colorMeshes,
    lineMeshes,
    scalarMeshFields,
    pointClouds,
    images,
    scalarFields,
    vectorFields
  )

  // this is a convenience method to add a statistical model as a (gp, mesh) combination.
  def addStatisticalMeshModel(model: StatisticalMeshModel, initialName: String): Unit = {
    // FIXME: this method does not check the return values of the shapeModelTransformations.add(*) methods.
    // If another SSM already exists in the same group, this is very likely to yield unexpected results (but without failing or indicating an error).
    // This method should either be replaced with a safer implementation, or maybe be removed altogether in favor of a different solution.
    //
    // NOTE: the following code:
    // genericTransformations.add(DiscreteLowRankGpPointTransformation(model.gp), initialName)
    // is not a satisfactory solution IMO, but (at best) a semi-functional workaround.

    triangleMeshes.add(model.referenceMesh, initialName)
    shapeModelTransformations.addPoseTransformation(PointTransformation.RigidIdentity)
    shapeModelTransformations.addGaussianProcessTransformation(DiscreteLowRankGpPointTransformation(model.gp))

  }

  override def remove(): Unit = parent.remove(this)
}

