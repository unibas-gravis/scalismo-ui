package plugin

import java.io.File

import breeze.linalg.DenseVector
import scalismo.geometry.{ Landmark, Point3D, _3D }
import scalismo.io.StatismoIO
import scalismo.registration.RigidTransformationSpace
import scalismo.ui.model.{ PointTransformation, StatusMessage }
import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

import scala.util.Random

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)
    statusBar.set(new StatusMessage("Warning", StatusMessage.Warning))
    statusBar.set(new StatusMessage("Error", StatusMessage.Error))
    statusBar.set(new StatusMessage("Question", StatusMessage.Question))
    statusBar.set("Hello World!")

    val model = StatismoIO.readStatismoMeshModel(new File("/home/langguth/AAA_data/face.h5")).get
    val mesh = model.referenceMesh
    val group = scene.groups.add("first")
    //scene.groups.add("second")

    group.landmarks.add(new Landmark("one", Point3D(0, 0, 130)), "one")
    group.landmarks.add(new Landmark("two", Point3D(20, 0, 150)), "two")
    group.landmarks.add(new Landmark("three", Point3D(-20, 0, 150)), "three")

    val gpt = PointTransformation.LowRankGpPointTransformation(model.gp.interpolateNearestNeighbor)
    val gp = group.transformations.add(gpt, "gp")
    val rigid = group.transformations.add(PointTransformation.Identity, "identity")

    val meshNode = group.triangleMeshes.add(mesh, "face")
    //val meshCopy = firstGroup.triangleMeshes.add(mesh, "copy")
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

    new Thread() {
      val rand = new Random()

      override def run(): Unit = {
        def sleep() = Thread.sleep(3000)

        def randomRigid() = {
          val randoms = (0 until 6).map(_ => rand.nextFloat() - .5f)
          RigidTransformationSpace[_3D]().transformForParameters(DenseVector(randoms.toArray))
        }

        def randomCoeffs() = {
          val randoms = (0 until model.gp.rank).map(_ => (rand.nextFloat() - .5f) * 7)
          DenseVector(randoms.toArray)
        }

        while (true) {
          sleep()

          gp.transformation = gp.transformation.copy(coefficients = randomCoeffs())
          sleep()

          rigid.transformation = randomRigid()
        }

      }
    }.start()
  }

}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
