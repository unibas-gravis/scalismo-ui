package plugin

import java.io.File

import scalismo.common.ScalarArray
import scalismo.io.MeshIO
import scalismo.mesh.ScalarMeshField
import scalismo.ui.model.StatusMessage
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)
    statusBar.set(new StatusMessage("Warning", StatusMessage.Warning))
    statusBar.set(new StatusMessage("Error", StatusMessage.Error))
    statusBar.set(new StatusMessage("Question", StatusMessage.Question))
    statusBar.set("Hello World!")

    new Thread() {
      override def run(): Unit = {
        def sleep() = Thread.sleep(1)

        sleep()
        val firstGroup = scene.groups.add("first")
        sleep()
        scene.groups.add("second")

        sleep()
        val mesh = MeshIO.readMesh(new File("/home/langguth/AAA_data/face.vtk")).get
        val meshNode = firstGroup.triangleMeshes.add(mesh, "face")
        sleep()

        val meshField: ScalarMeshField[Float] = new ScalarMeshField(mesh, ScalarArray(mesh.points.zipWithIndex.map { case (pt, idx) => idx.toFloat }.toArray))
        val meshFieldNode = firstGroup.scalarMeshFields.add(meshField, "Field")

      }
    }.start()
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
