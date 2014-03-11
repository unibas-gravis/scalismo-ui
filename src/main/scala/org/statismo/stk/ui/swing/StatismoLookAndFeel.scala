package org.statismo.stk.ui.swing

import scala.swing.{SimpleSwingApplication, Swing}
import javax.swing.{SwingUtilities, UIManager}

object StatismoLookAndFeel {
  def initializeWith(lookAndFeelClassName: String): Unit = Swing.onEDTWait {
    UIManager.setLookAndFeel(lookAndFeelClassName)
    val laf = UIManager.getLookAndFeel
    if (laf.getClass.getSimpleName.startsWith("Nimbus")) {
      val defaults = laf.getDefaults
      defaults.put("Tree.drawHorizontalLines", true)
      defaults.put("Tree.drawVerticalLines", true)
    }
  }
}

trait StatismoLookAndFeel extends SimpleSwingApplication {
  override def main(args: Array[String]) = {
    StatismoLookAndFeel.initializeWith(defaultLookAndFeelClassName)
    super.main(args)
  }

  def defaultLookAndFeelClassName: String = {
    val nimbus = UIManager.getInstalledLookAndFeels.filter(_.getName.equalsIgnoreCase("nimbus")).map(i => i.getClassName)
    if (!nimbus.isEmpty) nimbus.head else UIManager.getSystemLookAndFeelClassName
  }

  override def startup(args: Array[String]) {
    super.startup(args)
    SwingUtilities.updateComponentTreeUI(top.peer)
  }
}
