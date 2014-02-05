package org.statismo.stk.ui.swing

import org.statismo.stk.ui.Scene
import java.io.File
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.OpenSceneTreeObjectAction
import org.statismo.stk.ui.Loadable
import org.statismo.stk.ui.StatismoFrame
import org.statismo.stk.ui.StatismoApp
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component
import org.statismo.stk.ui.ShapeModel
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.StaticMesh

class SimpleViewer(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    if (args.length >= 1) {
      scene.load(args.head)
    }
  }
  
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneTreeObjectAction(loadFiles, "Open Mesh...", Seq(StaticMesh))))
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneTreeObjectAction(loadFiles, "Open Statistical Shape Model", Seq(ShapeModel))))
// menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneObjectAction(loadFiles)))
//  SceneObjectPropertiesPanel.availableViews = SceneObjectPropertiesPanel.availableViews

  def loadFiles(files: Seq[File], factories: Seq[Loadable[SceneTreeObject]]): Unit = {
    //scene.removeAllObjects()
    scene.tryLoad(files.map(f => f.getAbsolutePath()).toList, factories)
  }

  override lazy val workspacePanel: Component = {{
      val p =new WorkspacePanel(workspace)
      //p.layout(new Button("huhu")) = BorderPanel.Position.South
      p
    }
  }
  
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = { s: Scene => new SimpleViewer(s) })
  }
}

