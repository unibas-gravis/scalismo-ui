package plugin

import java.awt.Color
import java.io.File

import scalismo.io.MeshIO
import scalismo.ui.model.properties.NodeProperty.event.PropertyChanged
import scalismo.ui.model.properties.{ NodeProperty, ColorProperty }
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)
    statusBar.set("Hello World!")

    val firstGroup = scene.groups.add("first")
    scene.groups.add("second")

    val m = MeshIO.readMesh(new File("/home/langguth/AAA_data/face.vtk")).get
    val mn = firstGroup.triangleMeshes.add(m, "face")

    // demonstration of implicits syntax
    scene.groups(0).triangleMeshes.head.mesh

    listenTo(mn.color, mn.opacity)

    reactions += {
      case PropertyChanged(p) if p == mn.color => println(p)
      case PropertyChanged(p) if p == mn.opacity => println(p)
    }

    mn.color.value = Color.RED
    mn.opacity.value = -5
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
