package org.statismo.stk.ui.swing


import java.io.File

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.ui._
import org.statismo.stk.ui.swing.actions.LoadAction

import scala.async.Async.async
import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.MenuItem
import scala.util.{Success, Try}

class SimpleViewer(scene: Scene) extends StatismoFrame(scene) {

  implicit val theScene = scene

  override def startup(args: Array[String]): Unit = {
    val openItem = new MenuItem({
      def doLoad(file: File): Try[Unit] = {
        ShapeModel.tryCreate(file)(scene).map(ok => Success(()))
      }
      new LoadAction(doLoad, ShapeModel, "Open Statistical Shape Model...")
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
      // sample: add a point cloud
      val pointSeq = immutable.IndexedSeq(Point3D(0, 0, 0), Point3D(0, 100, 0), Point3D(100, 100, 0), Point3D(100, 0, 0), Point3D(0, 0, 100), Point3D(0, 100, 100), Point3D(100, 100, 100), Point3D(100, 0, 100))
      StaticPointCloud.createFromPeer(pointSeq, None, Some("Point Cloud"))
    }
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = {
      s: Scene => new SimpleViewer(s)
    })
  }
}

