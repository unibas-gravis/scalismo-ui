package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import scala.swing.Button
import scala.swing.Label
import scala.swing.Slider
import scala.swing.event.ValueChanged
import scala.swing.event.ButtonClicked
import scala.swing.GridBagPanel
import scala.swing.GridBagPanel.Anchor
import scala.swing.Component
import breeze.stats.distributions.Gaussian
import scala.swing.GridPanel
import scala.swing.Reactions
import scala.swing.event.Event
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.ShapeModelInstance
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer

class PrincipalComponentsPanel(val minValue: Float = -3.0f, val maxValue: Float = 3.0f, val granularity: Float = 10.0f) extends BorderPanel with SceneObjectPropertyPanel {
  val description = "Shape Parameters"

  val reset = new Button("Reset")
  val random = new Button("Random")
  listenTo(reset, random)

  val buttons = {
    val panel = new GridPanel(1, 2)
    panel.contents ++= Seq(reset, random)
    panel
  }

  layout(buttons) = North

  private case class Entry(val index: Int) {
    val label = new Label(index.toString)
    val slider = new Slider() {
      min = (minValue * granularity).toInt
      max = (maxValue * granularity).toInt
      name = index.toString
      value = 0
    }
    val value = new Label(labelFormat(0.0f))
  }

  private class Table extends GridBagPanel {
    var entries: Buffer[Entry] = new ArrayBuffer

    // need to redefine because add() is protected in superclass
    def add(comp: Component, position: Tuple2[Int, Int]) = {
      val const = pair2Constraints(position)
      const.ipadx = 10
      if (position._1 == 0) {
        const.anchor = Anchor.East
      } else if (position._1 == 2) {
        const.anchor = Anchor.West
      }
      super.add(comp, const)
    }
    add(new Label("Index"), (0, 0))
    add(new Label("Control"), (1, 0))
    add(new Label("Value"), (2, 0))

    setEntryCount(3)

    def setEntryCount(count: Int) {
      var changed = false;
      { count until entries.length }.reverse.foreach { idx =>
        changed = true
        val e = entries(idx)
        PrincipalComponentsPanel.this.deafTo(e.slider)
        peer.remove(e.value.peer)
        peer.remove(e.slider.peer)
        peer.remove(e.label.peer)
        entries.remove(idx)
      }
      entries.length until count foreach { idx =>
        changed = true
        val e = Entry(idx)
        add(e.label, (0, idx + 1))
        add(e.slider, (1, idx + 1))
        add(e.value, (2, idx + 1))
        entries.insert(idx, e)
        PrincipalComponentsPanel.this.listenTo(e.slider)
      }
      if (changed) revalidate
    }
  }

  private var model: Option[ShapeModelInstance] = None

  def labelFormat(value: Float) = f"$value%1.1f"

  def resetValues = {
    if (model.isDefined)
      model.get.coefficients = IndexedSeq.fill(model.get.coefficients.length)(0.0f)
  }

  def sanitize(value: Float) = {
    Math.min(maxValue, Math.max(value, minValue))
  }

  def setCoefficient(index: Int, value: Float) = {
    if (model.isDefined) {
      val coeffs = (model.get.coefficients.map(f => f)).toArray
      if (index >= 0 && index < coeffs.length) {
        coeffs(index) = sanitize(value)
        model.get.coefficients = coeffs
      }
    }
  }

  def randomizeValues = {
    if (model.isDefined) {
      val coeffs = model.get.coefficients.map { v =>
        sanitize(Gaussian(0, 1).draw().toFloat)
      }
      model.get.coefficients = coeffs
    }
  }

  private val table = new Table

  def updateDisplayedCoefficients() = {
    if (model.isDefined) {
      val coeffs = model.get.coefficients
      0 until coeffs.size map { i =>
        deafTo(table.entries(i).slider)

        val v = coeffs(i)
        table.entries(i).slider.value = (v * granularity).toInt
        table.entries(i).value.text = labelFormat(v)
        listenTo(table.entries(i).slider)
      }
    }
  }

  {
    val tablePanel = new BorderPanel {
      layout(table) = North
    }
    layout(tablePanel) = Center
  }

  case object Cleanup extends Event

  reactions += {
    case ValueChanged(s) => {
      val slider = s.asInstanceOf[Slider]
      val index = slider.name.toInt
      val value = (slider.value / granularity).toFloat
      setCoefficient(index, value)
    }
    case ButtonClicked(b) => {
      if (b == reset) {
        resetValues
      } else if (b == random) {
        randomizeValues
      }
    }
    case ShapeModelInstance.CoefficientsChanged(m) => {
      updateDisplayedCoefficients
    }
    case Cleanup => {
      if (model.isDefined) {
        deafTo(model.get)
        model = None
      }
    }
  }

  def setObject(obj: Option[SceneTreeObject]): Boolean = {
    publish(Cleanup)
    if (obj.isDefined && obj.get.isInstanceOf[ShapeModelInstance]) {
      model = Some(obj.get.asInstanceOf[ShapeModelInstance])
      listenTo(model.get)
      table.setEntryCount(model.get.coefficients.length)
      updateDisplayedCoefficients
      true
    } else {
      false
    }
  }
}