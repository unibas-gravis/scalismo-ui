package scalismo.ui.view

import javax.swing.{SwingUtilities, ToolTipManager, UIManager}

import scalismo.ui.util.EdtUtil

import scala.swing.SimpleSwingApplication

/**
  * Scalismo Look and Feel.
  *
  * By default, Scalismo tries to use the Nimbus L&F, and falls back to the System L&F if that doesn't work.
  */
object ScalismoLookAndFeel {
  lazy val DefaultLookAndFeelClassName: String = {
    val nimbus = UIManager.getInstalledLookAndFeels.filter(_.getName.equalsIgnoreCase("nimbus")).map(i => i.getClassName)
    if (nimbus.nonEmpty) nimbus.head else UIManager.getSystemLookAndFeelClassName
  }

  /**
    * Initializes the look and feel.
    * This tweaks a few settings of the L&F so that it behaves the way we need it.
    * @param lookAndFeelClassName class name of the L&F to use.
    */
  def initializeWith(lookAndFeelClassName: String): Unit = {
    EdtUtil.onEdtWait({
      UIManager.setLookAndFeel(lookAndFeelClassName)
      val laf = UIManager.getLookAndFeel
      if (laf.getClass.getSimpleName.startsWith("Nimbus")) {
        val defaults = laf.getDefaults
        defaults.put("Tree.drawHorizontalLines", true)
        defaults.put("Tree.drawVerticalLines", true)
      }
      UIManager.put("FileChooser.readOnly", true)
      ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false)
    })
  }
}

