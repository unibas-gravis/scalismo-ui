package plugin

import java.io.File

import scalismo.io.{ ImageIO, StatismoIO }
import scalismo.ui.control.NodeVisibility.RenderableNodeWithVisibility
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)

    val group = scene.groups.add("da group")
    val img = ImageIO.read3DScalarImage[Short](new File("/home/langguth/AAA_data/bladder.nii")).get.map(_.toFloat)
    group.images.add(img, "img")
    val model = StatismoIO.readStatismoMeshModel(new File("/home/langguth/AAA_data/face.h5")).get
    val mesh = model.referenceMesh
    val meshNode = group.triangleMeshes.add(mesh, "face")

    //    group.scalarMeshFields.add(new ScalarMeshField(mesh, ScalarArray(mesh.points.zipWithIndex.map { case (pt, idx) => idx.toFloat }.toArray)), "smf")
    //    group.pointClouds.add((0 to 5).map(_ * 10).map(x => Point3D(x, x, x)), "pc")

    //    val gpt = PointTransformation.LowRankGpPointTransformation(model.gp.interpolateNearestNeighbor)
    //    val gp = group.transformations.add(gpt, "Gaussian Process")
    //    val rigid = group.transformations.add(PointTransformation.Identity, "identity")

    //    val meshCopy = group.triangleMeshes.add(mesh, "copy")
    //    group.landmarks.add(new Landmark("one", Point3D(0, 0, 130)))
    //    group.landmarks.add(new Landmark("two", Point3D(20, 0, 150)))
    //    group.landmarks.add(new Landmark("three", Point3D(-20, 0, 150)))
    //
    if (false) {
      // yeah, I know you love these :-)
      // anyway, here are a few examples of how the visibility stuff could be used:
      val allViews = frame.perspective.viewports
      val oneView = allViews.head

      val meshV: RenderableNodeWithVisibility = meshNode

      //          // global visibility: as a Boolean
      //          val visibleGlobally: Boolean = meshNode.visible
      //          meshNode.visible = false
      //
      //          // visibility in a single viewport
      //          val visibleInOne: Boolean = meshNode.visible(oneView)
      //          meshNode.visible(oneView) = false
      //
      //          // visibility in a number of viewports
      //          val visibleInAll: Boolean = meshNode.visible(allViews)
      //          meshNode.visible(allViews) = false

    }
    //
    //    new Thread() {
    //      val rand = new Random()
    //
    //      override def run(): Unit = {
    //        def sleep() = {} //Thread.sleep(1000)
    //
    //        def randomRigid() = {
    //          val randoms = (0 until 6).map(_ => rand.nextFloat() - .5f)
    //          RigidTransformationSpace[_3D]().transformForParameters(DenseVector(randoms.toArray))
    //        }
    //
    //        def randomCoeffs() = {
    //          val randoms = (0 until model.gp.rank).map(_ => (rand.nextFloat() - .5f) * 7)
    //          DenseVector(randoms.toArray)
    //        }
    //
    //        while (true) {
    //          sleep()
    //          gp.transformation = gp.transformation.copy(coefficients = randomCoeffs())
    //
    //          //          sleep()
    //          //          rigid.transformation = randomRigid()
    //        }
    //
    //      }
    //    } //.start()

    perspective.resetAllCameras()
    // end of setup
  }

}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
