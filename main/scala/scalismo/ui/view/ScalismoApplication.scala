package scalismo.ui.view

import javax.swing.SwingUtilities

import scalismo.ui.model.Scene

import scala.swing.SimpleSwingApplication

/**
  * A Scalismo application is a [[SimpleSwingApplication]], having a [[ScalismoFrame]] as the top component, and using the [[ScalismoLookAndFeel]].
  *
  * This class takes care of initializing the Look and Feel, and setting up the frame with the command-line arguments.
  * @param top the main scalismo frame.
  * @param lookAndFeelClassName Swing Look and Feel. By default, the value of [[ScalismoLookAndFeel.DefaultLookAndFeelClassName]] is used.
  *
  */
class ScalismoApplication(override val top: ScalismoFrame, lookAndFeelClassName: String = ScalismoLookAndFeel.DefaultLookAndFeelClassName) extends SimpleSwingApplication {
  override def main(args: Array[String]) = {
    ScalismoLookAndFeel.initializeWith(lookAndFeelClassName)
    super.main(args)
  }

  override def startup(args: Array[String]) = {
    top.setup(args)
    super.startup(args)
    SwingUtilities.updateComponentTreeUI(top.peer)
  }
}

object ScalismoApplication {
  /**
    * ScalismoApplication factory method.
    *
    * All arguments have sensible defaults, and can selectively be overridden.
    *
    * @param args command-line arguments. Default value: an empty array.
    * @param frameConstructor method constructing a scalismo frame, given a scene. Default value: [[ScalismoFrame.DefaultConstructor]]
    * @param scene a [[Scene]] object representing the model the application will work with. By default, a new Scene is created.
    * @param lookAndFeelClassName Swing Look and Feel. By default, the value of [[ScalismoLookAndFeel.DefaultLookAndFeelClassName]] is used.
    * @return
    */
  def apply(args: Array[String] = Array.ofDim[String](0), frameConstructor: ScalismoFrame.Constructor = ScalismoFrame.DefaultConstructor, scene: Scene = new Scene, lookAndFeelClassName: String = ScalismoLookAndFeel.DefaultLookAndFeelClassName): ScalismoApplication = {
    val application = new ScalismoApplication(ScalismoFrame(scene, frameConstructor), lookAndFeelClassName)
    application.main(args)
    application
  }

  // only meant for Java wrapper
  def run(args: Array[String]): Unit = apply(args)
}
