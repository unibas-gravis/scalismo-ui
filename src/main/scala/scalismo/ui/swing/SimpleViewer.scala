package scalismo.ui.swing

import java.awt.Color
import java.io.File

import scalismo.common.{ DiscreteScalarField, ScalarArray }
import scalismo.geometry.{ Point, _3D }
import scalismo.io.{ MeshIO, StatismoIO }
import scalismo.ui._
import scalismo.ui.swing.actions.LoadAction

import scala.async.Async.async
import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.MenuItem
import scala.util.{ Success, Try }

class SimpleViewer(scene: Scene) extends ScalismoFrame(scene) {

  implicit val theScene = scene

  override def startup(args: Array[String]): Unit = {
    val openItem = new MenuItem({
      def doLoad(file: File): Try[Unit] = {
        ShapeModelView.tryCreate(file)(scene).map(ok => Success(()))
      }
      new LoadAction(doLoad, ShapeModelView, "Open Statistical Shape Model...")
    })
    this.menuBar.fileMenu.contents.insert(0, openItem)

    super.startup(args)

    args foreach { file =>
      async {
        scene.tryLoad(file)
      }
    }

    if (false) {
      /* for testing: infinitely cycle through all perspectives */
      val t = new Thread() {
        setDaemon(true)

        override def run() = {
          val perspectives = Perspectives.availablePerspectives.reverse
          while (true) {
            for (f <- perspectives) {
              Thread.sleep(10000)
              scene.perspective = f.apply()(scene)
            }
          }
        }
      }
      t.start()
    }

    if (false) {
      // example: add a point cloud
      val pointSeq = immutable.IndexedSeq(Point(0, 0, 0), Point(0, 100, 0), Point(100, 100, 0), Point(100, 0, 0), Point(0, 0, 100), Point(0, 100, 100), Point(100, 100, 100), Point(100, 0, 100))
      val pc = PointCloudView.createFromSource(pointSeq, None, Some("Point Cloud"))
      pc.color.update(Color.RED)
    }

    if (false) {
      // example: add a vector field
      val m = StatismoIO.readStatismoMeshModel(new File("/home/langguth/AAA_data/face.h5")).get
      val vf = m.gp.mean
      VectorFieldView.createFromSource(vf, None, Some("Vector field"))
    }

    if (false) {
      val m = MeshIO.readMesh(new File("/home/langguth/AAA_data/face.vtk")).get
      val df = new DiscreteScalarField[_3D, Float](m, ScalarArray(m.points.zipWithIndex.map { case (pt, idx) => idx.toFloat }.toArray))
      ScalarFieldView.createFromSource(df, None, Some("ScalarField"))
    }
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApp(args, frame = {
      s: Scene => new SimpleViewer(s)
    })
  }
}

