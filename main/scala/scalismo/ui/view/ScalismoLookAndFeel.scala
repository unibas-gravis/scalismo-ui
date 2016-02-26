package scalismo.ui.view

import javax.swing.{SwingUtilities, ToolTipManager, UIManager}

import scalismo.ui.util.EdtUtil

import scala.swing.SimpleSwingApplication

trait ScalismoLookAndFeel extends SimpleSwingApplication {
  override def main(args: Array[String]) = {
    ScalismoLookAndFeel.initializeWith(ScalismoLookAndFeel.defaultLookAndFeelClassName)
    super.main(args)
  }

  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    SwingUtilities.updateComponentTreeUI(top.peer)
  }
}

object ScalismoLookAndFeel {
  def defaultLookAndFeelClassName: String = {
    val nimbus = UIManager.getInstalledLookAndFeels.filter(_.getName.equalsIgnoreCase("nimbus")).map(i => i.getClassName)
    if (nimbus.nonEmpty) nimbus.head else UIManager.getSystemLookAndFeelClassName
  }

  def initializeWith(lookAndFeelClassName: String): Unit = {
    EdtUtil.onEdt({
      UIManager.setLookAndFeel(lookAndFeelClassName)
      val laf = UIManager.getLookAndFeel
      if (laf.getClass.getSimpleName.startsWith("Nimbus")) {
        val defaults = laf.getDefaults
        defaults.put("Tree.drawHorizontalLines", true)
        defaults.put("Tree.drawVerticalLines", true)
      }
      UIManager.put("FileChooser.readOnly", true)
      ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false)
    }, wait = true)
  }
}

