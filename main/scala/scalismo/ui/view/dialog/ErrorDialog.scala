package scalismo.ui.view.dialog

import java.io.{ PrintWriter, StringWriter }
import javax.swing.{ BorderFactory, JComponent, UIManager }

import scalismo.ui.resources.icons.ScalableIcon
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.{ MultiLineLabel, ScalableUI }

import scala.swing._
import scala.swing.event.Key

object ErrorDialog {
  def show(exception: Throwable, title: String = "Error", additionalMessage: String = "", iconOverride: Option[ScalableIcon] = None)(implicit frame: ScalismoFrame): Unit = {
    val dialog = new ErrorDialog(exception, title, additionalMessage, iconOverride)
    dialog.pack()
    dialog.centerOnScreen()
    dialog.okButton.requestFocus()
    dialog.visible = true
  }
}

class ErrorDialog(exception: Throwable, title: String, additionalMessage: String, iconOverride: Option[ScalableIcon])(implicit val frame: ScalismoFrame) extends Dialog(frame) {
  modal = true

  peer.setTitle(title)

  val main = new BorderPanel

  val icon = {
    val fallback = UIManager.getIcon("OptionPane.errorIcon")
    iconOverride.map(_.resize(fallback.getIconWidth, fallback.getIconHeight)).getOrElse(fallback)
  }

  val iconLabel = new Label("", icon, Alignment.Center)

  val placeHolderMessageLabel = {
    val textOption = Option(exception.getMessage)
    val text = textOption.getOrElse(exception.getClass.getName)
    new Label(text, icon, Alignment.Right)
  }

  val placeHolderAdditionalLabelOption: Option[Label] = additionalMessage match {
    case null => None
    case s if s.trim.length == 0 => None
    case text => Some(new Label(text, icon, Alignment.Right))
  }

  val messageLabel = {
    val fullText = placeHolderAdditionalLabelOption match {
      case Some(label) => s"${label.text}\n\n${placeHolderMessageLabel.text}"
      case None => ""
    }
    new MultiLineLabel(fullText)
  }

  val messagePanel = new BorderPanel {
    layout(iconLabel) = BorderPanel.Position.West
    layout(messageLabel) = BorderPanel.Position.Center
  }

  val stackTrace = {
    val trace = new StringWriter()
    exception.printStackTrace(new PrintWriter(trace))

    val area = new TextArea(trace.toString) {
      rows = 25
      columns = 80
      editable = false
    }
    new ScrollPane(area)
  }

  val placeholder = Component.wrap(new JComponent {
    override def getPreferredSize: Dimension = {
      val size = new Dimension
      size.height = 0
      size.width = placeHolderMessageLabel.preferredSize.width
      placeHolderAdditionalLabelOption.foreach { l =>
        size.width = Math.max(size.width, l.preferredSize.width)
      }
      size.width += iconLabel.preferredSize.width
      size.width = Math.min(stackTrace.preferredSize.width, size.width)
      size
    }
  })

  val detailsButton: Button = new Button(new Action("Show Details") {
    mnemonic = Key.D.id

    override def apply(): Unit = {
      main.layout(stackTrace) = BorderPanel.Position.Center
      detailsButton.visible = false
      val dialog = ErrorDialog.this
      dialog.pack()
      dialog.centerOnScreen()
    }
  })

  val okButton = new Button(new Action("OK") {
    mnemonic = Key.O.id

    override def apply(): Unit = dispose()
  })

  val buttons = new BorderPanel {
    layout(okButton) = BorderPanel.Position.East
    layout(detailsButton) = BorderPanel.Position.West
  }

  main.layout(messagePanel) = BorderPanel.Position.North
  main.layout(placeholder) = BorderPanel.Position.Center
  main.layout(buttons) = BorderPanel.Position.South

  main.border = {
    val px = ScalableUI.scale(5)
    BorderFactory.createEmptyBorder(px, px, px, px)
  }

  peer.getRootPane.setDefaultButton(okButton.peer)

  contents = main
}
