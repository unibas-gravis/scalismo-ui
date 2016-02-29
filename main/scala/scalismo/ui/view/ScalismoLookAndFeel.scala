package scalismo.ui.view

import javax.swing.{ ToolTipManager, UIManager }

import scala.util.Try

/**
 * Scalismo Look and Feel.
 */
object ScalismoLookAndFeel {
  /**
   * The (class name of the) default look and feel. This is automatically determined,
   * in the following order of preference: Nimbus, then System, then cross-platform.
   */
  lazy val DefaultLookAndFeelClassName: String = {
    val nimbus = "javax.swing.plaf.nimbus.NimbusLookAndFeel"
    def system = UIManager.getSystemLookAndFeelClassName
    Stream(nimbus, system).find { laf => Try(Class.forName(laf)).isSuccess }.getOrElse(UIManager.getCrossPlatformLookAndFeelClassName)
  }

  /**
   * Initializes the look and feel.
   * This tweaks a few settings of the L&F so that it looks and behaves the way we like it.
   *
   * @param lookAndFeelClassName class name of the L&F to use.
   */
  def initializeWith(lookAndFeelClassName: String): Unit = {
    UIManager.setLookAndFeel(lookAndFeelClassName)
    //TODO: not sure if these things really have to be done on the EDT. If they actually do, uncomment the block.
    //EdtUtil.onEdtWait{
    val laf = UIManager.getLookAndFeel
    if (laf.getClass.getSimpleName.startsWith("Nimbus")) {
      val defaults = laf.getDefaults
      defaults.put("Tree.drawHorizontalLines", true)
      defaults.put("Tree.drawVerticalLines", true)
    }
    UIManager.put("FileChooser.readOnly", true)
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false)
    HighDpi.updateLookAndFeelDefaults()
    //}
  }
}

