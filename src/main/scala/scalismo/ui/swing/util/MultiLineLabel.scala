package scalismo.ui.swing.util

import java.awt.Color
import javax.swing.{ BorderFactory, UIManager }

import scala.swing.TextArea

class MultiLineLabel(text: String) extends TextArea(text) {
  peer.setLineWrap(true)
  peer.setWrapStyleWord(true)
  peer.setEditable(false)
  peer.setCursor(null)
  peer.setOpaque(false)
  peer.setFocusable(false)
  peer.setBackground(new Color(UIManager.getColor("control").getRGB))
  val hv = ScalableUI.scale(10)
  peer.setBorder(BorderFactory.createEmptyBorder(hv, hv, hv, hv))

}
