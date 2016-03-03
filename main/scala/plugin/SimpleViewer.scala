package plugin

import java.awt.Color
import java.io.File

import scalismo.io.MeshIO
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)
    statusBar.set("Hello World!")

    new Thread() {
      override def run(): Unit = {
        def sleep() = Thread.sleep(1)

        sleep()
        val firstGroup = scene.groups.add("first")
        sleep()
        scene.groups.add("second")

        sleep()
        val m = MeshIO.readMesh(new File("/home/langguth/AAA_data/face.vtk")).get
        val mn = firstGroup.triangleMeshes.add(m, "face")
        listenTo(mn.color, mn.opacity)
        sleep()
        firstGroup.triangleMeshes.add(m, "copy")
        sleep()
        mn.color.value = Color.RED
        sleep()
        mn.opacity.value = .2f
      }
    }.start()
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
