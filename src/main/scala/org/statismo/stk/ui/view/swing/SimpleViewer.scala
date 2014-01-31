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
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component

class SimpleViewer(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    if (args.length >= 1) {
      scene.loadObjects(args.head)
    }
  }
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneObjectAction(loadFiles, "Open mesh...", Seq(RawMesh))))
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneObjectAction(loadFiles, "Open shape model", Seq(StatModel))))
// menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneObjectAction(loadFiles)))
//  SceneObjectPropertiesPanel.availableViews = SceneObjectPropertiesPanel.availableViews

  def loadFiles(files: Seq[File], factories: Seq[Loadable[SceneObject]]): Unit = {
    //scene.removeAllObjects()
    scene.loadObjects(files.map(f => f.getAbsolutePath()).toList, factories)
  }

  override lazy val workspacePanel: Component = {
    val p =new WorkspacePanel(workspace)
    p.layout(new Button("huhu")) = BorderPanel.Position.South
    p
    //new Button("X")
  }
  
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = { s: Scene => new SimpleViewer(s) })
  }
}

