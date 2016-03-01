package plugin

import java.io.File

import scalismo.io.MeshIO
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)
    statusBar.set("Hello World!")

    val firstGroup = scene.groups.add("first")
    scene.groups.add("second")

    val m = MeshIO.readMesh(new File("/home/langguth/AAA_data/face.vtk")).get
    val mn = firstGroup.triangleMeshes.add(m, "face")

    // demonstration of implicits
    scene.groups(0).triangleMeshes.head.mesh
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
