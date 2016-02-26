package scalismo.ui.view

import scalismo.ui.model.Scene

import scala.swing.Button

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(args, {scene => new SimpleViewer(scene)})
  }
}

class SimpleViewer(scene: Scene) extends ScalismoFrame(scene) {
  override def setup(args: Array[String]): Unit = {
    super.setup(args)
    println("setup done")
  }
}
