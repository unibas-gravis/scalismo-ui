package scalismo.ui.view

import scalismo.ui.util.EdtUtil

import scala.swing.SimpleSwingApplication

/**
 * A Scalismo application is a [[SimpleSwingApplication]], having a [[ScalismoFrame]] as the top component, and using the [[ScalismoLookAndFeel]].
 *
 * This class takes care of initializing the Look and Feel, and setting up the frame with the command-line arguments.
 *
 * @param frame                the main scalismo frame. This value is passed "by name", so instantiation happens on demand, and on the correct thread (Swing EDT)
 * @param lookAndFeelClassName Swing Look and Feel. By default, the value of [[ScalismoLookAndFeel.DefaultLookAndFeelClassName]] is used.
 *
 */
class ScalismoApplication(frame: => ScalismoFrame, lookAndFeelClassName: String = ScalismoLookAndFeel.DefaultLookAndFeelClassName) extends SimpleSwingApplication {

  override lazy val top = EdtUtil.onEdtWait(frame)

  override def main(args: Array[String]) = {
    scalismo.initialize()
    ScalismoLookAndFeel.initializeWith(lookAndFeelClassName)
    super.main(args)
  }

  override def startup(args: Array[String]) = {
    top.setup(args)
    super.startup(args)
  }
}

object ScalismoApplication {

  /**
   * ScalismoApplication factory method.
   *
   * All arguments have sensible defaults, and can selectively be overridden.
   *
   * @param frame                the top-level frame. This value is passed "by name", so instantiation happens on demand, and on the correct thread (Swing EDT).
   * @param args                 command-line arguments. Default value: an empty array.
   * @param lookAndFeelClassName Swing Look and Feel. By default, the value of [[ScalismoLookAndFeel.DefaultLookAndFeelClassName]] is used.
   * @return
   */
  def apply(frame: => ScalismoFrame = new ScalismoFrame, args: Array[String] = new Array(0), lookAndFeelClassName: String = ScalismoLookAndFeel.DefaultLookAndFeelClassName): ScalismoApplication = {
    val application = new ScalismoApplication(frame, lookAndFeelClassName)
    application.main(args)
    application
  }

  // only meant for the trivial Java wrapper
  def run(args: Array[String]): Unit = apply(args = args)
}
