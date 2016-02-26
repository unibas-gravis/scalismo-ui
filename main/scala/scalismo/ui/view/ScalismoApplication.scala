package scalismo.ui.view

import scalismo.ui.model.Scene

import scala.swing.SimpleSwingApplication

object ScalismoApplication {

  def apply(args: Array[String] = Array.ofDim[String](0), frameConstructor: ScalismoFrame.Constructor = ScalismoFrame.defaultConstructor, scene: Scene = new Scene, lookAndFeelClassName: String = ScalismoLookAndFeel.defaultLookAndFeelClassName): ScalismoApplication = {
    val app = new ScalismoApplication(ScalismoFrame.apply(scene, frameConstructor))
    app.main(args)
    app
  }
}

class ScalismoApplication(override val top: ScalismoFrame) extends SimpleSwingApplication with ScalismoLookAndFeel {
  override def startup(args: Array[String]) = {
    top.setup(args)
    super.startup(args)
  }
}

