package org.statismo.stk.ui.swing

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
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = {
      s: Scene => new SimpleViewer(s)
    })
  }
}

