package scalismo.ui.swing.util

import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.{ Color, Cursor, Desktop }
import java.net.URI
import javax.swing.Icon

import scala.swing.Swing.EmptyIcon
import scala.swing.{ Alignment, Label }

object LinkLabel {

  lazy val desktop: Option[Desktop] = {
    if (!Desktop.isDesktopSupported) None
    else {
      val desktop = Option(Desktop.getDesktop)
      if (desktop.nonEmpty && desktop.forall(_.isSupported(Desktop.Action.BROWSE))) desktop else None
    }
  }
}

class LinkLabel(text: String, uri: URI, icon: Icon = EmptyIcon, alignment: Alignment.Value = Alignment.Center, preventLinkStyle: Boolean = false, preventTooltip: Boolean = false) extends Label(text, icon, alignment) {
  if (!preventTooltip) {
    tooltip = uri.toString
  }

  // this will only kick in if the desktop can actually open links
  LinkLabel.desktop.foreach { d =>
    if (!preventLinkStyle) {
      foreground = Color.BLUE.darker()
      //      val attributes = font.getAttributes
      //      attributes.asInstanceOf[java.util.Map[Object, Object]].put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
      //      font = font.deriveFont(attributes)
    }

    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = {
        if (e.getClickCount == 1) {
          d.browse(uri)
        }
      }
    })
  }
}
