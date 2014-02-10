package org.statismo.stk.ui.swing

import org.statismo.stk.ui.Scene
import java.io.File
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.OpenSceneTreeObjectAction
import org.statismo.stk.ui.StatismoFrame
import org.statismo.stk.ui.StatismoApp
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component
import org.statismo.stk.ui.ShapeModel
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.SceneTreeObjectFactory
import org.statismo.stk.ui.StaticMesh
import java.awt.Color

class SimpleViewer(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    if (args.length >= 1) {
      scene.load(args.head)
    }
  }
  
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneTreeObjectAction(loadFiles, "Open Mesh...", Seq(StaticMesh))))
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneTreeObjectAction(loadFiles, "Open Statistical Shape Model...", Seq(ShapeModel))))
  
  def loadFiles(files: Seq[File], factories: Seq[SceneTreeObjectFactory[SceneTreeObject]]): Unit = {
    val x = scene.tryLoad(files.map(f => f.getAbsolutePath()).toList, factories)
    x foreach {o =>
      if (o.isSuccess) {
        if (o.get.isInstanceOf[ShapeModel]) {
          val s = o.get.asInstanceOf[ShapeModel]
          s.instances.children(0).meshRepresentation.color = Color.RED
        }
      }
    }
  }

//  override lazy val workspacePanel: WorkspacePanel = {{
//      val p =new WorkspacePanel(workspace)
//      p
//    }
//  }
  
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = { s: Scene => new SimpleViewer(s) })
  }
}

