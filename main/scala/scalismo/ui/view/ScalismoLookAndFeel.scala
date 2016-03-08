package scalismo.ui.view

import javax.swing.{ ToolTipManager, UIDefaults, UIManager }

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

    ScalableUI.updateLookAndFeelDefaults()

    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false)

    allDefaults.foreach(_.put("FileChooser.readOnly", true))
    replaceDialogIcons()

    val laf = UIManager.getLookAndFeel
    if (laf.getClass.getSimpleName.startsWith("Nimbus")) {
      val nimbus = laf.getDefaults
      nimbus.put("Tree.drawHorizontalLines", true)
      nimbus.put("Tree.drawVerticalLines", true)
    }
  }

  /*
  * It's not always clear whether setting the global defaults also affects the actual LAF.
  * I've seen some weird race conditions happening. This method returns both the global
  * and the LAF defaults, so that changes can be applied to both as necessary.
  */
  private def allDefaults: List[UIDefaults] = List(UIManager.getDefaults, UIManager.getLookAndFeel.getDefaults)

  private def replaceDialogIcons(): Unit = {
    import scalismo.ui.resources.icons.BundledIcon._

    val replacements = List((Information, "information"), (Warning, "warning"), (Error, "error"), (Question, "question"))
    replacements.foreach {
      case (icon, partialName) =>
        val key = s"OptionPane.${partialName}Icon"
        allDefaults.foreach { defaults =>
          Option(defaults.getIcon(key)).foreach { original =>
            val replacement = ScalableUI.resizeIcon(icon, original.getIconWidth, original.getIconHeight)
            defaults.put(key, replacement)
          }
        }
    }
  }
}

