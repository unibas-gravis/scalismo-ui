package plugin

import java.io.File

import breeze.linalg.DenseVector
import scalismo.geometry.{ Point, _3D }
import scalismo.io.StatismoIO
import scalismo.registration.RigidTransformationSpace
import scalismo.ui.api.SimpleAPI
import scalismo.ui.model.DiscreteLowRankGpPointTransformation
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)

    //    group.addStatisticalMeshModel(model, "face")

    //    val meshField = ScalarMeshField(model.referenceMesh, ScalarArray(model.referenceMesh.pointIds.map(_.id.toFloat).toArray))
    //    group.scalarMeshFields.add(meshField, "ptIds")

    perspective.resetAllCameras()

    val staticGroup = scene.groups.add("static")

  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
