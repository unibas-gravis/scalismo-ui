package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.ui.Scene.ImageWindowLevel
import scalismo.ui.swing.util.FancySlider
import scalismo.ui.{ Image3DView, Scene }

import scala.swing._
import scala.swing.event.ValueChanged

class ImageWindowLevelPanel extends BorderPanel with PropertyPanel {
  override def description: String = "Window/Level"

  private var target: Option[ImageWindowLevel] = None

  private val windowSlider = new FancySlider
  private val levelSlider = new FancySlider

  def updateSlidersMax(nmax: Int): Unit = {
    windowSlider.max = nmax
    levelSlider.max = nmax
  }

  val allowDecreaseCheckbox = new CheckBox("allow decreasing the current value ")

  val recalculateMaxButton = new Button(new Action("Recalculate slider maximum") {
    override def apply(): Unit = target.foreach { wl =>
      var max = -1.0f
      def seen(f: Float): Unit = {
        max = Math.max(max, f)
      }

      wl.scene.find[Image3DView[_]]().foreach { v => v.asFloatImage.foreach(seen) }
      val maxInt = Math.ceil(max).toInt

      if (maxInt > windowSlider.max) {
        updateSlidersMax(maxInt)
      } else if (maxInt > 0 && maxInt < windowSlider.max && allowDecreaseCheckbox.selected) {
        updateSlidersMax(maxInt)
      }
    }
  })

  {
    val northedPanel = new BorderPanel {
      val windowPanel = new BorderPanel {
        border = new TitledBorder(null, "Window", TitledBorder.LEADING, 0, null, null)
        layout(windowSlider) = BorderPanel.Position.Center
      }
      val levelPanel = new BorderPanel {
        border = new TitledBorder(null, "Level", TitledBorder.LEADING, 0, null, null)
        layout(levelSlider) = BorderPanel.Position.Center
      }
      val buttonsPanel = new BorderPanel() {
        layout(recalculateMaxButton) = BorderPanel.Position.West
        layout(allowDecreaseCheckbox) = BorderPanel.Position.East
      }

      val slidersPanel = new BorderPanel {
        layout(windowPanel) = BorderPanel.Position.North
        layout(levelPanel) = BorderPanel.Position.Center
        layout(buttonsPanel) = BorderPanel.Position.South
      }
      layout(slidersPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

  listenToOwnEvents()

  def listenToOwnEvents() = {
    listenTo(windowSlider, levelSlider)
  }

  def deafToOwnEvents() = {
    deafTo(windowSlider, levelSlider)
  }

  def updateUi() = {
    target.foreach { t =>
      deafToOwnEvents()
      val max = Math.ceil(Math.max(t.window, t.level)).toInt
      if (max > windowSlider.max) {
        updateSlidersMax(max)
      }
      val (w, l) = (Math.round(t.window).toInt, Math.round(t.level).toInt)
      if (windowSlider.value != w) {
        windowSlider.value = w
      }
      if (levelSlider.value != l) {
        levelSlider.value = l
      }
      listenToOwnEvents()
    }
  }

  override def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    val r = obj match {
      case Some(s: Scene) =>
        target = Some(s.imageWindowLevel)
        listenTo(s)
        updateUi()
        true
      case _ => false
    }
    updateEnabledState()
    r
  }

  def cleanup(): Unit = {
    target.foreach(t => deafTo(t.scene))
    target = None
  }

  def updateEnabledState(): Unit = {
    def enableAll(on: Boolean): Unit = {
      Seq(windowSlider, levelSlider, allowDecreaseCheckbox, recalculateMaxButton).foreach(_.enabled = on)
    }
    val on = target.map(_.scene.find[Image3DView[_]]().nonEmpty)
    enableAll(on.getOrElse(false))
  }

  reactions += {
    case Scene.TreeTopologyChanged(_) => updateEnabledState()
    case ImageWindowLevel.ImageWindowLevelChanged(_, w, l) => updateUi()
    case ValueChanged(c) if c eq windowSlider => target.foreach(t => t.window = windowSlider.value)
    case ValueChanged(c) if c eq levelSlider => target.foreach(t => t.level = levelSlider.value)
  }
}
