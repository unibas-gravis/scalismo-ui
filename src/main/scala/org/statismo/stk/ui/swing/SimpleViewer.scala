package org.statismo.stk.ui.swing


import javax.media.opengl.Threading

import org.statismo.stk.ui._
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.LoadAction
import java.io.File
import scala.util.{Success, Try}

class SimpleViewer(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
    val openItem = new MenuItem({
      def doLoad(file: File): Try[Unit] = {
        ShapeModel.tryCreate(file)(scene).map(ok => Success(()))
      }
      new LoadAction(doLoad, ShapeModel, "Open Statistical Shape Model...")
    })
    this.menuBar.fileMenu.contents.insert(0, openItem)

    super.startup(args)

    args foreach {
      scene.tryLoad(_)
    }

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
    //t.start()
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
//    org.statismo.stk.core.initialize()
//    playground.JoglConeRendering.main(args)
    StatismoApp(args, frame = {
      s: Scene => new SimpleViewer(s)
    })
  }
}

