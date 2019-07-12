package scalismo.ui.view.ScalarBarWindow

import java.io.File

import scalismo.io.MeshIO
import scalismo.ui.api.ScalismoUI

object popup{

  // we now need to load the scalismo viewer here to test
  scalismo.initialize()
  implicit val rng = scalismo.utils.Random(42)

  val ui = ScalismoUI()


  def main(args: Array[String]): Unit = {
    // the file path with the color
    val filepath = "C:\\Users\\Normand\\Documents\\software_engineering\\scalismo-ui\\src\\main\\scala\\scalismo\\ui\\view\\ScalarBarWindow\\femur-color.vtk"
    val mesh = MeshIO.readScalarMeshFieldAsType[Float](new File(filepath)).get
    val modelGroup = ui.createGroup("testGroup")
    val modelView = ui.show(modelGroup, mesh, "colorMesh")

    //val popup = new ScalarBar()
    //popup.create()
  }
}



