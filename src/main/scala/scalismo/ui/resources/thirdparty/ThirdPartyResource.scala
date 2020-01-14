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

package scalismo.ui.resources.thirdparty

import java.util.Scanner

import scalismo.ui.resources.thirdparty.breeze.Breeze
import scalismo.ui.resources.thirdparty.elusive.Elusive
import scalismo.ui.resources.thirdparty.entypo.Entypo
import scalismo.ui.resources.thirdparty.fontawesome.FontAwesome
import scalismo.ui.resources.thirdparty.interpreterpane.InterpreterPane
import scalismo.ui.resources.thirdparty.javagraphics.JavaGraphics
import scalismo.ui.resources.thirdparty.jhdf.Jhdf
import scalismo.ui.resources.thirdparty.jiconfont.JIconFont
import scalismo.ui.resources.thirdparty.jogl.Jogl
import scalismo.ui.resources.thirdparty.niftijio.NiftiJio
import scalismo.ui.resources.thirdparty.spire.Spire
import scalismo.ui.resources.thirdparty.spray.Spray
import scalismo.ui.resources.thirdparty.vtk.Vtk

object ThirdPartyResource {
  val All: List[ThirdPartyResource] = List(Vtk,
                                           Jogl,
                                           Jhdf,
                                           Breeze,
                                           NiftiJio,
                                           Spire,
                                           Spray,
                                           InterpreterPane,
                                           JavaGraphics,
                                           JIconFont,
                                           FontAwesome,
                                           Elusive,
                                           Entypo).sortBy(_.name.toLowerCase)
}

abstract class ThirdPartyResource {
  def name: String

  def authors: String

  def homepage: Option[String]

  def licenseText: Option[String] = readLicense()

  def licenseName: String

  def readLicense(): Option[String] = readResource("license.txt")

  def readResource(resourceName: String): Option[String] = {
    val resource = Option(this.getClass.getResourceAsStream(resourceName))
    resource.flatMap { stream =>
      val scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A")
      if (scanner.hasNext) {
        Some(scanner.next())
      } else {
        None
      }
    }
  }
}
