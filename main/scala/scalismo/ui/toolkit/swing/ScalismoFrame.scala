package scalismo.ui.toolkit.swing

import scalismo.ui.model.Scene

import scala.swing.MainFrame

object ScalismoFrame {
  type Constructor = (Scene => ScalismoFrame)

  def defaultConstructor: Constructor = {
    s: Scene => new ScalismoFrame(s)
  }

  def apply(scene: Scene = new Scene, constructor: Constructor = defaultConstructor): ScalismoFrame = {
    EdtUtil.onEdtWithResult(constructor(scene))
  }
}

class ScalismoFrame(val scene: Scene) extends MainFrame {

  def startup(args:Array[String]): Unit = {}

}
