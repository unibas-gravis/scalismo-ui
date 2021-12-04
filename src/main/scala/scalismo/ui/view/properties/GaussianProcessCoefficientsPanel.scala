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

package scalismo.ui.view.properties

import java.awt.event.{MouseAdapter, MouseEvent}

import breeze.linalg.DenseVector
import breeze.stats.distributions.Gaussian
import javax.swing.JSlider
import scalismo.ui.model.{LowRankGpPointTransformation, PointTransformation, SceneNode, TransformationNode}
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.ScalableUI.implicits.scalableInt

import scala.collection.mutable
import scala.swing.GridBagPanel.{Anchor, Fill}
import scala.swing._
import scala.swing.event.{ButtonClicked, ValueChanged}

object GaussianProcessCoefficientsPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = new GaussianProcessCoefficientsPanel(frame)

  val MaxAbsoluteCoefficientValue: Float = 3.0f

  val CoefficientValueStep: Float = 0.1f
}

class GaussianProcessCoefficientsPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  val description = "Coefficients"

  val reset = new Button("Mean")
  val random = new Button("Random")
  listenTo(reset, random)

  private val buttons = {
    val panel = new GridPanel(1, 2)
    panel.contents ++= Seq(reset, random)
    panel
  }

  layout(buttons) = BorderPanel.Position.North

  private case class Entry(index: Int) {
    val label = new Label(index.toString)
    val slider: Slider = new Slider {
      override lazy val peer: JSlider with SuperMixin = new JSlider with SuperMixin {
        // this tries to jump directly to the value the user clicked.
        addMouseListener(new MouseAdapter() {
          override def mousePressed(e: MouseEvent): Unit = {
            val p = e.getPoint
            val percent = p.x / getWidth.toDouble
            val range = getMaximum - getMinimum
            val newVal = range * percent
            val result = (getMinimum + newVal).toInt
            setValue(result)
          }
        })
      }
      max =
        (GaussianProcessCoefficientsPanel.MaxAbsoluteCoefficientValue / GaussianProcessCoefficientsPanel.CoefficientValueStep).toInt
      min = -max
      name = index.toString
      this.value = 0
      snapToTicks = true
    }
    val value = new Label(labelFormat(0.0f))
  }

  private class Table extends GridBagPanel {
    val entries: mutable.Buffer[Entry] = new mutable.ArrayBuffer

    // need to redefine because add() is protected in superclass
    def add(comp: Component, position: (Int, Int)): Unit = {
      val const = pair2Constraints(position)
      const.ipadx = 10.scaled
      if (position._1 == 0) {
        const.anchor = Anchor.East
      } else if (position._1 == 2) {
        const.anchor = Anchor.West
      } else {
        const.weightx = 1
        const.fill = Fill.Horizontal
      }
      super.add(comp, const)
    }

    add(new Label("Index"), (0, 0))
    add(new Label("Control"), (1, 0))
    add(new Label("Value"), (2, 0))

    setEntryCount(3)

    def setEntryCount(count: Int): Unit = {
      var changed = false;
      {
        count until entries.length
      }.reverse.foreach { idx =>
        changed = true
        val e = entries(idx)
        GaussianProcessCoefficientsPanel.this.deafTo(e.slider)
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
        GaussianProcessCoefficientsPanel.this.listenTo(e.slider)
      }
      if (changed) revalidate()
    }
  }

  private var node: Option[TransformationNode[LowRankGpPointTransformation]] = None

  def labelFormat(value: Double) = f"$value%1.1f"

  def resetValues(): Unit = {
    node.foreach { n =>
      n.transformation = n.transformation.copy(coefficients = DenseVector.zeros[Double](n.transformation.gp.rank))
    }
  }

  private def setCoefficient(index: Int, value: Double): Unit = {
    node.foreach { n =>
      val coeffs = n.transformation.coefficients.toArray
      if (coeffs(index) != value) {
        coeffs(index) = value
        n.transformation = n.transformation.copy(coefficients = DenseVector(coeffs))
      }
    }
  }

  def randomizeValues(): Unit = {
    import breeze.stats.distributions.Rand.FixedSeed.randBasis
    node.foreach { n =>
      val coeffs = n.transformation.coefficients.toArray
      coeffs.indices.foreach { index =>
        val value = Gaussian(0, 1).draw().toFloat
        coeffs(index) = value
      }
      n.transformation = n.transformation.copy(coefficients = DenseVector(coeffs))
    }
  }

  private val table = new Table

  def updateDisplayedCoefficients(): Unit = {
    node.foreach { n =>
      val coeffs = n.transformation.coefficients.toArray
      coeffs.indices foreach { i =>
        deafTo(table.entries(i).slider)

        val v = coeffs(i)
        table.entries(i).slider.value = (v / GaussianProcessCoefficientsPanel.CoefficientValueStep).toInt
        table.entries(i).value.text = labelFormat(v)

        listenTo(table.entries(i).slider)
      }
    }
  }

  layout(new BorderPanel {
    layout(table) = BorderPanel.Position.North
  }) = BorderPanel.Position.Center

  reactions += {
    case ValueChanged(slider: Slider) =>
      val index = slider.name.toInt
      val value = slider.value * GaussianProcessCoefficientsPanel.CoefficientValueStep
      setCoefficient(index, value)
    case ButtonClicked(`reset`)  => resetValues()
    case ButtonClicked(`random`) => randomizeValues()

    case TransformationNode.event.TransformationChanged(_) =>
      updateDisplayedCoefficients()
  }

  def cleanup(): Unit = {
    node.foreach { n =>
      deafTo(n)
      node = None
    }
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    // we have to account for type erasure, that's why we need the collect
    singleMatch[TransformationNode[_ <: PointTransformation]](nodes).collect {
      case tn if tn.transformation.isInstanceOf[LowRankGpPointTransformation] =>
        tn.asInstanceOf[TransformationNode[LowRankGpPointTransformation]]
    } match {
      case None => false
      case Some(tn) =>
        node = Some(tn)
        listenTo(tn)
        table.setEntryCount(tn.transformation.gp.rank)
        updateDisplayedCoefficients()
        true
    }
  }
}
