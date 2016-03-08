package scalismo.ui.resources.thirdparty

import java.util.Scanner

import scalismo.ui.resources.thirdparty.batik.Batik
import scalismo.ui.resources.thirdparty.breeze.Breeze
import scalismo.ui.resources.thirdparty.interpreterpane.InterpreterPane
import scalismo.ui.resources.thirdparty.javagraphics.JavaGraphics
import scalismo.ui.resources.thirdparty.jhdf.Jhdf
import scalismo.ui.resources.thirdparty.jogl.Jogl
import scalismo.ui.resources.thirdparty.linea.Linea
import scalismo.ui.resources.thirdparty.niftijio.NiftiJio
import scalismo.ui.resources.thirdparty.spire.Spire
import scalismo.ui.resources.thirdparty.spray.Spray
import scalismo.ui.resources.thirdparty.svgicon.SvgIcon
import scalismo.ui.resources.thirdparty.vtk.Vtk

object ThirdPartyResource {
  val All: List[ThirdPartyResource] = List(Vtk, Jogl, Jhdf, Breeze, NiftiJio, Linea, Spire, Spray, InterpreterPane, JavaGraphics, SvgIcon, Batik).sortBy(_.name.toLowerCase)
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
