package plugin

import java.io.File

import breeze.linalg.DenseVector
import scalismo.geometry.{Point, _3D}
import scalismo.io.StatismoIO
import scalismo.registration.RigidTransformationSpace
import scalismo.ui.model.DiscreteLowRankGpPointTransformation
import scalismo.ui.view.{ScalismoApplication, ScalismoFrame}

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)

    val group = scene.groups.add("a group")
    val model = StatismoIO.readStatismoMeshModel(new File("/home/bouabene/000/femurMooc.h5")).get


    //    group.addStatisticalMeshModel(model, "face")

    //    val meshField = ScalarMeshField(model.referenceMesh, ScalarArray(model.referenceMesh.pointIds.map(_.id.toFloat).toArray))
    //    group.scalarMeshFields.add(meshField, "ptIds")


    perspective.resetAllCameras()

   // val smt = ShapeModelTransformation(RigidTransformationSpace[_3D].transformForParameters(DenseVector(1,1,1,1,1,1)), DiscreteLowRankGpPointTransformation(model.gp))

    group.shapeModelTransformations.addPoseTransformation(RigidTransformationSpace[_3D].transformForParameters(DenseVector(1,1,1,1,1,1)), "shapemodel rigid")
    group.shapeModelTransformations.addGaussianProcessTransformation(DiscreteLowRankGpPointTransformation(model.gp), "shapemmodel GP")


    // try to add the transform again
    group.shapeModelTransformations.addGaussianProcessTransformation(DiscreteLowRankGpPointTransformation(model.gp), "shapemmodel GP")


    group.triangleMeshes.add(model.referenceMesh, "ref")

    group.genericTransformations.add(RigidTransformationSpace[_3D].transformForParameters(DenseVector(1,1,1,1,1,1)), "more rigid")

    group.genericTransformations.add((pt: Point[_3D]) => model.referenceMesh.pointSet.findClosestPoint(pt).point, "closest Point proj")

    val staticGroup = scene.groups.add("static")
    staticGroup.triangleMeshes.add(model.referenceMesh, "a")
}
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
