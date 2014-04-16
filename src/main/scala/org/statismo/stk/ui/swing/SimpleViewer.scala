package org.statismo.stk.ui.swing

import org.statismo.stk.ui.{Scene, StatismoApp, StatismoFrame}

class SimpleViewer(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
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

