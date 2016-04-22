package plugin

import java.io.File

import scalismo.io.StatismoIO
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)

    val group = scene.groups.add("a group")
    val model = StatismoIO.readStatismoMeshModel(new File("/home/langguth/AAA_data/face.h5")).get
    group.addStatisticalMeshModel(model, "face")

    //    val meshField = ScalarMeshField(model.referenceMesh, ScalarArray(model.referenceMesh.pointIds.map(_.id.toFloat).toArray))
    //    group.scalarMeshFields.add(meshField, "ptIds")
    perspective.resetAllCameras()
  }

}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
