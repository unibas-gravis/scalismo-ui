package usecases

import org.statismo.stk.ui.Scene
import java.io.File
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.OpenSceneTreeObjectAction
import org.statismo.stk.ui.SceneTreeObjectFactory
import org.statismo.stk.ui.StatismoFrame
import org.statismo.stk.ui.StatismoApp
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component
import org.statismo.stk.ui.ShapeModel
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.StaticMesh
import java.awt.Color
import org.statismo.stk.ui.DisplayableLandmarks
import org.statismo.stk.ui.swing.WorkspacePanel
import org.statismo.stk.ui.Landmarks

class Varian(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    if (args.length >= 1) {
      scene.load(args.head)
    }
  }

  this.workspacePanel.asInstanceOf[WorkspacePanel].layout(new Button("X")) = BorderPanel.Position.East
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneTreeObjectAction(loadMesh, "Open Mesh...", Seq(StaticMesh), false)))
  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneTreeObjectAction(loadModel, "Open Statistical Shape Model...", Seq(ShapeModel), false)))

  private var meshLm: DisplayableLandmarks = null
  private var modelLm: DisplayableLandmarks = null
  private var orgModel: Option[ShapeModel] = None
  private var lastModel: Option[ShapeModel] = None

  implicit val theScene = this.scene

  def loadMesh(files: Seq[File], factories: Seq[SceneTreeObjectFactory[SceneTreeObject]]): Unit = {
    val x = scene.tryLoad(files.map(f => f.getAbsolutePath()).toList, factories)
    x foreach { o =>
      if (o.isSuccess) {
        val s = o.get.asInstanceOf[StaticMesh]
        meshLm = s.parent.parent.landmarks
        listenTo(meshLm)
        s.color = Color.GREEN
      }
    }
  }

  def loadModel(files: Seq[File], factories: Seq[SceneTreeObjectFactory[SceneTreeObject]]): Unit = {
    files foreach { fn =>
      val o = ShapeModel(fn, 1)
      if (o.isSuccess) {
        orgModel = Some(o.get)
        lastModel = Some(o.get)
        o.get.instances(0).meshRepresentation.color = Color.RED
        modelLm = o.get.instances(0).landmarks
        listenTo(modelLm)
      }
    }
  }

  reactions += {
    case Landmarks.LandmarksChanged(lm) => {
      if (lm == modelLm || lm == meshLm) {
        if (modelLm != null && meshLm != null) {
          if (modelLm.children.length == meshLm.children.length) {
            val refLms = lastModel.get.landmarks.children.map(_.peer).toIndexedSeq
            val targetLms = meshLm.children.map(_.peer).toIndexedSeq
            val trainingData = refLms.zip(targetLms).map { case (refPt, tgtPt) => (refPt, tgtPt - refPt) }
            val posteriorModel = orgModel.get.peer.posterior(trainingData, 2, true)
            val nm = ShapeModel(posteriorModel, orgModel)

            if (lastModel.get eq orgModel.get) {
              orgModel.get.remove()
            } else {
              lastModel.get.remove()
              nm.landmarks.children.foreach(_.remove)
              lastModel.get.landmarks.children.foreach { lm => nm.landmarks.create(lm.peer) }
            }
            lastModel = Some(nm)

            deafTo(modelLm)
            modelLm = nm.instances(0).landmarks
            listenTo(modelLm)
          }
        }
      }
    }
  }

}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = { s: Scene => new Varian(s) })
  }
}

