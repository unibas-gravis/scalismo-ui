package scalismo.ui.toolkit.swing

import scalismo.ui.model.Scene

import scala.swing.Button

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(args, frameConstructor = {
      s: Scene => new SimpleViewer(s)
    })
  }
}

class SimpleViewer(scene: Scene) extends ScalismoFrame(scene) {

  this.contents = new Button("ASDF")
}
