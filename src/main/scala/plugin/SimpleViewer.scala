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

package plugin

import java.io.File

import breeze.linalg.DenseVector
import scalismo.common.{ PointId, UnstructuredPointsDomain }
import scalismo.geometry.{ Point3D, Point, _3D }
import scalismo.io.StatismoIO
import scalismo.mesh.{ LineId, LineCell, LineList, LineMesh }
import scalismo.registration.RigidTransformationSpace
import scalismo.ui.api.SimpleAPI
import scalismo.ui.model.DiscreteLowRankGpPointTransformation
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)

    def spiral(t: Double): Point[_3D] = {
      val R = 5;
      val a = 1;
      val x = R * Math.cos(t)
      val y = R * Math.sin(t)
      val z = a * t;
      Point3D(x, y, z)
    }

    val group = scene.groups.add("mesh")
    val spiralPoints = for (t <- 0.0 until 10.0 by 0.1) yield spiral(t)

    val domain = UnstructuredPointsDomain(spiralPoints.toIndexedSeq)

    val cells = for (id <- 0 until spiralPoints.size - 1) yield LineCell(PointId(id), PointId(id + 1))
    val topology = LineList(cells.toIndexedSeq)
    val lineMesh = LineMesh(domain, topology)

    group.lineMeshes.add(lineMesh, "abc")

    perspective.resetAllCameras()

  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
