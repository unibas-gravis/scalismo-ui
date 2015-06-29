package scalismo.ui.swing

import scalismo.ui.Scene

import scala.swing._

object ScalismoApp {

  import scalismo.ui.swing.ScalismoFrame.FrameConstructor

  def defaultFrameConstructor: FrameConstructor = {
    s: Scene => new ScalismoFrame(s)
  }

  def apply(args: Array[String] = new Array[String](0), scene: Scene = new Scene, frame: FrameConstructor = defaultFrameConstructor, lookAndFeelClassName: String = ScalismoLookAndFeel.defaultLookAndFeelClassName): ScalismoApp = {
    ScalismoLookAndFeel.initializeWith(lookAndFeelClassName)
    val app = new ScalismoApp(ScalismoFrame(frame, scene))
    app.main(args)
    app
  }
}

class ScalismoApp(val top: ScalismoFrame) extends SimpleSwingApplication {
  override def startup(args: Array[String]) = {
    top.startup(args)
    super.startup(args)
  }

  def scene = top.scene

}

