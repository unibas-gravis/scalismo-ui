package org.statismo.stk.ui.swing

import org.statismo.stk.ui.{Perspectives, Scene, StatismoApp, StatismoFrame}
import scala.swing.Swing
import vtk.vtkObjectBase
import java.util.concurrent.TimeUnit
import org.statismo.stk.ui.visualization.SphereLike
import org.statismo.stk.ui.visualization.props.HasColorAndOpacity

class SimpleViewer(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    args foreach {
      scene.tryLoad(_)
    }

    new Thread() {
      override def run() = {
        while (!disposed) {
          Perspectives.availablePerspectives.foreach {
            f =>
              if (!disposed) {
                Thread.sleep(5000)
                Swing.onEDTWait {
                  scene.perspective = f.apply()(scene)
                }
              }
          }
        }
      }
    }//.start()
  }

  var disposed = false

  override def dispose = {
    val x = vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector
    x.Stop()
    disposed = true
    super.dispose
  }

}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = {
      s: Scene => new SimpleViewer(s)
    })
  }
}

