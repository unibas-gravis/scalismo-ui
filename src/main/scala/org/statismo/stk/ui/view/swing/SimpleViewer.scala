package org.statismo.stk.ui.view.swing

import org.statismo.stk.ui.Scene
import java.io.File
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.OpenSceneObjectAction
import org.statismo.stk.ui.Loadable
import org.statismo.stk.ui.SceneObject
import org.statismo.stk.ui.RawMesh
import org.statismo.stk.ui.StatModel
import org.statismo.stk.ui.StatismoFrame
import org.statismo.stk.ui.StatismoApp

class SimpleViewer(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    if (args.length >= 1) {
      scene.loadObjects(args.head)
    }
  }

  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneObjectAction(loadFiles, "Open mesh...", Seq(RawMesh))))
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneObjectAction(loadFiles, "Open shape model", Seq(StatModel))))
//  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneObjectAction(loadFiles)))

  def loadFiles(files: Seq[File], factories: Seq[Loadable[SceneObject]]): Unit = {
    //scene.removeAllObjects()
    scene.loadObjects(files.map(f => f.getAbsolutePath()).toList, factories)
  }

}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = { s: Scene => new SimpleViewer(s) })
  }
}

