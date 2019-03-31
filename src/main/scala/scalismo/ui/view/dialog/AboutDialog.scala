/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.view.dialog

import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.{ Color, Cursor, Font }
import java.net.URI

import javax.swing._
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.resources.thirdparty.ThirdPartyResource
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.dialog.AboutDialog._
import scalismo.ui.view.dialog.AboutDialog.scaled._
import scalismo.ui.view.util.{ LinkLabel, MultiLineLabel, ScalableUI }

import scala.swing.GridBagPanel.{ Anchor, Fill }
import scala.swing.Swing.EmptyIcon
import scala.swing.TabbedPane.Page
import scala.swing.{ Action, _ }
import scala.util.Try

object AboutDialog {

  /**
   * This is essentially a workaround for IntelliJ Idea not liking
   * auto-generated sources. (I.e. Idea continues to bitch about
   * not finding scalismo.ui.BuildInfo, thus making it impossible
   * to compile or start the program from the UI).
   *
   * Using runtime reflection avoids this.
   */
  object BuildInfo {

    import scala.reflect.runtime.universe

    lazy val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    lazy val objectName = "scalismo.ui.BuildInfo$"

    def proxy(fieldName: String): String = Try {
      val moduleSymbol = runtimeMirror.moduleSymbol(
        Class.forName(objectName)
      )

      val targetMethod = moduleSymbol.typeSignature
        .members
        .filter(x => x.isMethod && x.name.toString == fieldName)
        .head
        .asMethod

      runtimeMirror.reflect(runtimeMirror.reflectModule(moduleSymbol).instance)
        .reflectMethod(targetMethod)().toString
    }.getOrElse("???")

    // proxied fields
    def version: String = proxy("version")

    def scalaVersion: String = proxy("scalaVersion")

    def sbtVersion: String = proxy("sbtVersion")

    def buildTime: String = proxy("buildTime")
  }

  object scaled {

    import ScalableUI.implicits.scalableInt

    /* scaled versions of pixel sizes.
    * Defined separately (instead of just using a base, and a multiplier / divisor)
    * because they're mostly small values, and the scale factor is generally also a small value
    * (between 1 and 2) -- so (for example) 3.scaled * 2 is not necessarily equal to (3 * 2).scaled .
    *
    * Defined as methods in subpackage to avoid code duplication (in other words: all components should use one of
    * the values defined here, not a literal value)
     */

    def s_3 = 3.scaled

    def s_5 = 5.scaled

    def s_10 = 10.scaled

    def s_15 = 15.scaled

    def s_20 = 20.scaled

    def s_128 = 128.scaled
  }

  class BoldLabel(text: String, icon: Icon = EmptyIcon, alignment: Alignment.Value = Alignment.Center) extends Label(text, icon, alignment) {
    font = font.deriveFont(font.getStyle | Font.BOLD)
  }

  class LogoPanel extends BorderPanel {
    val logo = BundledIcon.Logo
    val dim = s_128
    val scaledIcon = ScalableUI.resizeIcon(logo, dim, dim)

    val image = new LinkLabel("", new URI("https://github.com/unibas-gravis/scalismo"), scaledIcon, Alignment.Left, preventLinkStyle = true, preventTooltip = true) {
      peer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
    }

    val h = s_15
    image.border = BorderFactory.createEmptyBorder(h * 2, h, h * 2, h)

    layout(image) = BorderPanel.Position.North
  }

  class KeyValuePanel(pairs: List[(String, String)]) extends GridBagPanel {
    private var x = 0
    private var y = 0
    val columns = 2

    def constraints(text: String) = new Constraints {
      this.gridx = x
      this.gridy = y
      this.anchor = Anchor.NorthWest
      this.ipadx = s_20
      this.ipady = s_3

      if (text == null) {
        // special "Fill" entry: fill vertical space
        this.weighty = 1
      }

      // update state for next add()
      x += 1
      if (x == columns) {
        x = 0
        y += 1
      }
    }

    pairs.foreach {
      case (key, value) =>
        add(new BoldLabel(key), constraints(key))
        add(new Label(value), constraints(value))
    }
  }

  class ThirdPartyPanel(frame: ScalismoFrame) extends BorderPanel {
    val description = "The scalismo library, and the user interface, use a number of third-party open source resources. These dependencies are listed below."

    val descriptionBorder = BorderFactory.createEmptyBorder(s_10, 0, s_10, 0)
    val thanksBorder = BorderFactory.createEmptyBorder(s_10, 0, 0, 0)
    val listBorder = BorderFactory.createEmptyBorder(s_3, s_10, s_3, s_10)
    val scrollBorder = BorderFactory.createEmptyBorder(0, 0, 0, s_10)

    val north = new MultiLineLabel(description) {
      border = descriptionBorder
    }

    val center: BorderPanel = new BorderPanel {
      val list = new ThirdPartyListPanel(frame) {
        border = listBorder
      }
      val scroll = new ScrollPane(list) {
        horizontalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
        verticalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
      }
      scroll.border = BorderFactory.createCompoundBorder(scrollBorder, scroll.border)
      layout(scroll) = BorderPanel.Position.Center
    }
    layout(north) = BorderPanel.Position.North
    layout(center) = BorderPanel.Position.Center

    // very swiss, but it doesn't harm, and I bet it will be appreciated.
    layout(new BoldLabel("Thank you!", alignment = Alignment.Left) {
      border = thanksBorder
    }) = BorderPanel.Position.South
  }

  class ThirdPartyListPanel(frame: ScalismoFrame) extends GridBagPanel {
    private var x = 0
    private var y = 0
    val columns = List("Name", "Author(s)", "License")
    val lastColumnIndex = columns.length - 1
    val lastRowIndex = ThirdPartyResource.All.length + 1 // accounts for header and footer

    def constraints() = new Constraints {
      this.gridx = x
      this.gridy = y
      this.anchor = Anchor.NorthWest
      this.ipadx = s_20
      this.ipady = s_3

      this.weightx = x match {
        case c if c == lastColumnIndex => 1
        case _ => 0
      }

      this.weighty = y match {
        case r if r == lastRowIndex => 1
        case _ => 0
      }

      this.fill = (x, y) match {
        case (h, v) if h == lastColumnIndex && v == lastRowIndex => Fill.Both
        case (h, _) if h == lastColumnIndex => Fill.Horizontal
        case (_, v) if v == lastRowIndex => Fill.Vertical
        case (_, _) => Fill.None
      }

      // update state for next add()
      x += 1
      if (x == columns.length) {
        x = 0
        y += 1
      }
    }

    def add(label: Label): Unit = {
      label.horizontalAlignment = Alignment.Left
      add(label, constraints())
    }

    // table headers
    columns.foreach(c => add(new BoldLabel(c)))

    ThirdPartyResource.All.foreach { tp =>
      val name = tp.homepage match {
        case Some(url) => new LinkLabel(tp.name, new URI(url))
        case None => new Label(tp.name)
      }
      val author = new Label(tp.authors)
      val license = tp.licenseText match {
        case Some(licenseText) => new Label(tp.licenseName) {
          tooltip = "Show License"
          foreground = Color.BLUE.darker()
          peer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
          peer.addMouseListener(new MouseAdapter {
            override def mouseClicked(e: MouseEvent): Unit = {
              popupLicense(frame, tp.name, licenseText)
            }
          })
        }
        case None => new Label(tp.licenseName)
      }

      add(name)
      add(author)
      add(license)
    }

    // table footer (dummy row to fill space on resize)
    (0 to lastColumnIndex).foreach { _ => add(new Label("")) }
  }

  def popupLicense(frame: ScalismoFrame, productName: String, licenseText: String): Unit = {
    val dialog = new Dialog(owner = frame) {
      modal = true
      title = s"$productName - License"
    }

    val main = new BorderPanel
    val button = new BorderPanel {
      layout(new Button(new Action("OK") {
        override def apply(): Unit = dialog.dispose()
      })) = BorderPanel.Position.East
    }

    val _rows = 25
    val _columns = 80

    val textComponent = {
      if (licenseText.startsWith("<html>")) {
        // set the same preferred width as a 25x80 text area
        val prefWidth = new TextArea("", _rows, _columns).preferredSize.width
        val pane = new JTextPane() {
          setContentType("text/html")
          setText(licenseText)
          setEditable(false)

          override def getPreferredSize: Dimension = {
            val real = super.getPreferredSize
            real.width = prefWidth
            real
          }

        }
        Component.wrap(pane)
      } else {
        new TextArea(licenseText) {
          rows = _rows
          columns = _columns
          editable = false
          lineWrap = true
          peer.setWrapStyleWord(true)
        }
      }
    }

    val scroll = new ScrollPane(textComponent)

    main.layout(scroll) = BorderPanel.Position.Center
    main.layout(button) = BorderPanel.Position.South

    dialog.contents = main
    dialog.pack()
    dialog.centerOnScreen()
    dialog.visible = true

  }

}

class AboutDialog(implicit frame: ScalismoFrame) extends Dialog(frame) {
  modal = true
  title = "About Scalismo UI"

  def withLogo(component: Component) = new BorderPanel {
    layout(component) = BorderPanel.Position.Center
    layout(new LogoPanel) = BorderPanel.Position.West
  }

  val main: BorderPanel = new BorderPanel {

    val keyValue = List(
      "Developers" -> "<html>Ghazi Bouabene<br/>Thomas Gerig<br/>Christoph Langguth<br/>Marcel Lüthi</html>",
      (null, null), // special: fill
      "UI Version" -> BuildInfo.version,
      "Scalismo Version" -> scalismo.BuildInfo.version,
      "Scala Version" -> BuildInfo.scalaVersion,
      "Build Time" -> BuildInfo.buildTime
    )

    val kvPanel: BorderPanel = new BorderPanel {
      val west = new BorderPanel {
        layout(new KeyValuePanel(keyValue)) = BorderPanel.Position.Center
      }

      layout(west) = BorderPanel.Position.West
      layout(new LinkLabel("Copyright (c) Graphics and Vision Research Group, University of Basel", new URI("http://gravis.cs.unibas.ch/"), alignment = Alignment.Left, preventLinkStyle = true, preventTooltip = true) {
        val b = s_10
        border = BorderFactory.createEmptyBorder(b, b, b, b)
      }) = BorderPanel.Position.North
    }

    val tabsPane = new TabbedPane

    // we explicitly use "null" for tooltips, as the default scala.swing implementation uses "", which displays a useless tooltip
    tabsPane.pages += new Page("Scalismo UI", withLogo(kvPanel), null)
    tabsPane.pages += new Page("Third Party", withLogo(new ThirdPartyPanel(frame)), null)

    val bottomPanel: BorderPanel = new BorderPanel {
      val ok = new Button(new Action("OK") {
        override def apply(): Unit = AboutDialog.this.dispose()
      })
      layout(new BorderPanel {
        layout(ok) = BorderPanel.Position.Center
        val b = s_5
        border = BorderFactory.createEmptyBorder(b, b, b, b)
      }) = BorderPanel.Position.East
    }

    layout(tabsPane) = BorderPanel.Position.Center
    layout(bottomPanel) = BorderPanel.Position.South

    border = BorderFactory.createEmptyBorder(s_5, 0, 0, 0)
  }

  contents = main
  pack()
  centerOnScreen()
  visible = true
}
