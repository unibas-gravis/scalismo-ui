package org.statismo.stk.ui.view.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Orientation
import org.statismo.stk.ui.StatModel
import scala.swing.Label
import scala.swing.Slider
import scala.swing.event.ValueChanged
import scala.swing.event.ButtonClicked
import scala.swing.ScrollPane
import scala.swing.GridBagPanel
import scala.swing.GridBagPanel.Anchor
import scala.swing.Component
import scala.util.Random
import breeze.stats.distributions.Gaussian
import scala.swing.GridPanel
import javax.swing.border.TitledBorder
import org.statismo.stk.ui.view.PrincipalComponentsView
import org.statismo.stk.ui.controller.PrincipalComponentsController
import org.statismo.stk.ui.controller.PrincipalComponentsController
import org.statismo.stk.ui.controller.PrincipalComponentsController
import scala.swing.Reactions
import org.statismo.stk.ui.SceneObject
import scala.swing.event.Event

class PrincipalComponentsPanel(val minValue: Float = -3.0f, val maxValue: Float = 3.0f, val granularity: Float = 10.0f) extends BorderPanel with SceneObjectPropertyView {
  //controller.views += PrincipalComponentsPanel.this
  //border = new TitledBorder(null, "Shape Coefficients", TitledBorder.LEADING, TitledBorder.BELOW_TOP, null, null)

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

  private class GBP extends GridBagPanel {
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
  }

  def labelFormat(value: Float) = f"$value%1.1f"

  def sanitize(value: Float) = {
    Math.min(maxValue, Math.max(value, minValue))
  }

  //  private var model: Option[StatModel] = None
  private var reaction: Option[Reactions.Reaction] = None

  case object Cleanup extends Event
  def setObject(obj: Option[SceneObject]): Boolean = {
    publish(Cleanup)
    if (obj.isDefined && obj.get.isInstanceOf[StatModel]) {
      updateUi(obj.get.asInstanceOf[StatModel])
      true
    } else {
      false
    }
  }

  def benchmark[R](block: => R, desc: String = "duration"): R = {
    val start = System.currentTimeMillis()
    val result = block
    //println(desc + ": " + (System.currentTimeMillis() - start) + " ms")
    result
  }

  def updateUi(model: StatModel) {
    val labels = model.coefficients.map(f => new Label(labelFormat(f)))

    val sliders = benchmark({
      val panel = new GBP()
      panel.add(new Label("Index"), (0, 0))
      panel.add(new Label("Control"), (1, 0))
      panel.add(new Label("Value"), (2, 0))
      val sliders = 0 until labels.size map { index =>
        panel.add(new Label(index.toString), (0, index + 1))
        val slider = new Slider() {
          min = (minValue * granularity).toInt
          max = (maxValue * granularity).toInt
          name = index.toString
          value = (granularity * model.coefficients(index)).toInt
        }
        panel.add(slider, (1, index + 1))
        panel.add(labels(index), (2, index + 1))
        listenTo(slider)
        slider
      }
      val northed = new BorderPanel {
        layout(panel) = North
      }
      //      layout(new ScrollPane(northed) {
      //        horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
      //        verticalScrollBarPolicy = ScrollPane.BarPolicy.Always
      //      }) = Center
      layout(northed) = Center
      sliders
    },"cslid");
    listenTo(model, random, reset)
    updateCoefficients()

    reaction = Some({
      case ValueChanged(s) => benchmark({
        val slider = s.asInstanceOf[Slider]
        val index = slider.name.toInt
        val value = (slider.value / granularity).toFloat
        setCoefficient(index, value)
      },"vc")
      case ButtonClicked(b) => {
        if (b == reset) {
          resetValues
        } else if (b == random) {
          randomizeValues
        }
      }
      case SceneObject.GeometryChanged => benchmark({
        updateCoefficients()
      },"sgc")
      case Cleanup => benchmark({
        benchmark({
        deafTo(model, random, reset)
        sliders foreach (deafTo(_))
        }, "deafto")
        benchmark({
        if (reaction.isDefined) {
          reactions -= reaction.get
          reaction = None
        }}, "rr")
        //benchmark(layout(new Label()) = Center)
        benchmark(Seq(random, reset).foreach(_.enabled = false))
      },"cleanup")
    })

    reactions += reaction.get
    Seq(random, reset).foreach(_.enabled = true)

    def setCoefficient(index: Int, value: Float) = {
      val coeffs = (model.coefficients.map(f => f)).toArray
      if (index >= 0 && index < coeffs.length) {
        coeffs(index) = sanitize(value)
        model.coefficients = coeffs
      }
    }

    def resetValues = {
      model.coefficients = IndexedSeq.fill(model.coefficients.length)(0.0f)
    }

    def randomizeValues = {
      val coeffs = model.coefficients.map { v =>
        sanitize(Gaussian(0, 1).draw().toFloat)
      }
      model.coefficients = coeffs
    }

    def updateCoefficients() = {
      val coeffs = model.coefficients
      0 until coeffs.size map { i =>
        deafTo(sliders(i))

        val v = coeffs(i)
        sliders(i).value = (v * granularity).toInt
        labels(i).text = labelFormat(v)

        listenTo(sliders(i))
      }
    }
  }
}