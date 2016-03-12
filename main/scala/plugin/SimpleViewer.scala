package plugin

import java.io.File

import scalismo.io.MeshIO
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
        val meshCopy = firstGroup.triangleMeshes.add(mesh, "copy")
        if (false) {
          // yeah, I know you love these :-)
          // anyway, here are a few examples of how the visibility stuff could be used:
          val allViews = frame.perspectivesPanel.viewports
          val oneView = allViews.head

          // global visibility: as a Boolean
          val visibleGlobally: Boolean = meshNode.visible
          meshNode.visible = false

          // visibility in a single viewport
          val visibleInOne: Boolean = meshNode.visible(oneView)
          meshNode.visible(oneView) = false

          // visibility in a number of viewports
          val visibleInAll: Boolean = meshNode.visible(allViews)
          meshNode.visible(allViews) = false

        }
        sleep()

      }
    }.start()
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
