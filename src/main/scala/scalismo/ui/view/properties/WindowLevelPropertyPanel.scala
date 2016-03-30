package scalismo.ui.view.properties

import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.border.TitledBorder
import javax.swing.{ BorderFactory, JComponent }

import scalismo.ui.model.properties.NodeProperty
import scalismo.ui.model.{ ImageNode, SceneNode }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.properties.WindowLevelPropertyPanel.Showit
import scalismo.ui.view.util.FancySlider
import scalismo.ui.view.util.ScalableUI.implicits.scalableInt

import scala.swing.Swing.EmptyIcon
import scala.swing._
import scala.swing.event.ValueChanged

object WindowLevelPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = new WindowLevelPropertyPanel(frame)

  class Showit extends JComponent {

    import java.awt._

    var (min, max, level, window) = (-100, 100, 50, 50)

    override def getPreferredSize: Dimension = {
      new Dimension(50, 50)
    }

    override def paint(g: Graphics): Unit = {
      super.paint(g)

      val size = getSize()
      val image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB)

      // from a value in the "image range" (min-max), calculate an x position in this component
      def a(v: Float): Int = {
        val percent = (v - min) / (max - min)
        Math.round(size.width * percent)
      }

      val lineWidth = {
        val proposed = 3.scaled
        // ensure it's an odd value
        if (proposed % 2 == 1) proposed else proposed + 1
      }

      val g2 = image.getGraphics.asInstanceOf[Graphics2D]
      g2.setStroke(new BasicStroke(1))

      // window
      val wl = a(level - window / 2.0f)
      val wr = a(level + window / 2.0f)
      val ww = wr - wl

      // left of window: everything BLACK
      g2.setColor(Color.BLACK)
      g2.fillRect(0, 0, wl, size.height)

      // right of window: everything WHITE
      g2.setColor(Color.WHITE)
      g2.fillRect(wr, 0, size.width - wr, size.height)

      (wl to wr).foreach { x =>
        // wr: 255, wl: 0
        val intensity = (x - wl).toFloat / ww
        g2.setColor(new Color(intensity, intensity, intensity))
        g2.drawLine(x, 0, x, size.height)
      }

      g2.setStroke(new BasicStroke(lineWidth))

      // level bar
      g2.setColor(Color.GREEN.darker)
      val l = a(level)
      g2.drawLine(l, 0, l, size.height)

      // border around window
      g2.drawRect(wl, 0, ww, size.height)

      g2.dispose()
      g.drawImage(image, 0, 0, size.width, size.height, null)
    }
  }

}

class WindowLevelPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Window/Level"

  var targets: List[ImageNode] = Nil

  val levelSlider = new FancySlider
  val windowSlider = new FancySlider

  val showit = new Showit

  def cleanup(): Unit = {
    targets.headOption.foreach(n => deafTo(n.windowLevel))
  }

  def initUi(): Unit = {
    deafToOwnEvents()

    levelSlider.min = Math.ceil(targets.map(_.minimumValue).min).toInt
    levelSlider.max = Math.floor(targets.map(_.maximumValue).max).toInt
    windowSlider.min = 0
    windowSlider.max = levelSlider.max - levelSlider.min

    updateUi()

    listenToOwnEvents()
  }

  def updateUi(): Unit = {
    deafToOwnEvents()

    val wl = targets.head.windowLevel.value

    windowSlider.value = Math.round(wl.window)
    levelSlider.value = Math.round(wl.level)

    updateImage()

    listenToOwnEvents()
  }

  def updateImage(): Unit = {
    showit.min = levelSlider.min
    showit.max = levelSlider.max
    showit.level = levelSlider.value
    showit.window = windowSlider.value
    showit.repaint()
  }

  def listenToOwnEvents(): Unit = {
    listenTo(levelSlider, windowSlider)
  }

  def deafToOwnEvents(): Unit = {
    deafTo(levelSlider, windowSlider)
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val images = allMatch[ImageNode](nodes)
    if (images.nonEmpty) {
      targets = images
      listenTo(images.head.windowLevel)
      initUi()
      true
    } else {
      false
    }
  }

  reactions += {
    case ValueChanged(s: Slider) if s eq windowSlider => targets.foreach { n =>
      val updated = n.windowLevel.value.copy(window = windowSlider.value)
      n.windowLevel.value = updated
    }
    case ValueChanged(s: Slider) if s eq levelSlider => targets.foreach { n =>
      val updated = n.windowLevel.value.copy(level = levelSlider.value)
      n.windowLevel.value = updated
    }
    case NodeProperty.event.PropertyChanged(_) => updateUi()
  }

  // layout
  {
    val inset = 10.scaled
    val northedPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      val slidersPanel = new GridBagPanel {
        val constraints = new Constraints() {
          ipadx = inset
        }
        var (x, y) = (0, 0)

        def next: Constraints = {
          constraints.gridx = x
          constraints.gridy = y

          if (x == 0) {
            constraints.weightx = 0
            constraints.fill = GridBagPanel.Fill.None
            constraints.anchor = GridBagPanel.Anchor.West
          } else {
            constraints.weightx = 1
            constraints.fill = GridBagPanel.Fill.Horizontal
            constraints.anchor = GridBagPanel.Anchor.Center
          }

          // prepare for next call
          x += 1
          if (x == 2) {
            x = 0
            y += 1
          }
          constraints
        }

        add(new Label("Level", EmptyIcon, Alignment.Leading), next)
        add(levelSlider, next)
        add(new Label("Window", EmptyIcon, Alignment.Leading), next)
        add(windowSlider, next)
      }

      val showitWrap = new BorderPanel {
        layout(Component.wrap(showit)) = BorderPanel.Position.Center
        border = {
          val line = BorderFactory.createLineBorder(Color.DARK_GRAY, 3.scaled, true)
          val empty = BorderFactory.createEmptyBorder(0, 0, inset, 0)
          BorderFactory.createCompoundBorder(empty, line)
        }
      }

      layout(showitWrap) = BorderPanel.Position.North
      layout(slidersPanel) = BorderPanel.Position.Center
    }

    layout(northedPanel) = BorderPanel.Position.North
  }

}
