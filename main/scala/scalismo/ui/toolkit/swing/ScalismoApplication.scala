package scalismo.ui.toolkit.swing

import scalismo.ui.model.Scene
import scalismo.ui.toolkit.swing.ScalismoFrame.Constructor

import scala.swing.SimpleSwingApplication

object ScalismoApplication {

  def apply(args: Array[String] = new Array[String](0), scene: Scene = new Scene, frameConstructor: Constructor = ScalismoFrame.defaultConstructor, lookAndFeelClassName: String = ScalismoLookAndFeel.defaultLookAndFeelClassName): ScalismoApplication = {
    val app = new ScalismoApplication(ScalismoFrame(scene, frameConstructor))
    app.main(args)
    app
  }
}

class ScalismoApplication(override val top: ScalismoFrame) extends SimpleSwingApplication with ScalismoLookAndFeel {
  override def startup(args: Array[String]) = {
    top.startup(args)
    super.startup(args)
  }

//  def scene = top.scene

}

