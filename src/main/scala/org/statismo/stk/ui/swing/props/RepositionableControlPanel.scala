package org.statismo.stk.ui.swing.props


import scala.swing._

import org.statismo.stk.ui.{IndirectlyRepositionable, DirectlyRepositionable, Axis, Repositionable}
import org.statismo.stk.ui.Repositionable.Amount
import scala.Some
import scala.util.Try
import org.statismo.stk.core.geometry.Point3D
import scala.swing.GridBagPanel.{Anchor, Fill}


class RepositionableControlPanel extends BorderPanel with PropertyPanel {
  override val description = "Position"

  private var target: Option[Repositionable] = None

  def cleanup() {
    target.map {
      r =>
        deafTo(r)
    }
    target = None
    table.setFooter(NoItem)
  }

  override def setObject(objOption: Option[AnyRef]) = {
    cleanup()
    objOption match {
      case Some(r: Repositionable) =>
        target = Some(r)
        listenTo(r)
        updateUi()
        true
      case _ => false
    }
  }

  reactions += {
    case Repositionable.CurrentPositionChanged(t) if target != None && target.get == t => updateCoordinates()
  }

  def updateUi() = {
    target match {
      case Some(d: DirectlyRepositionable) =>
        table.setFooter(UpdatePositionButton)
        Axes.foreach(t => t._2.enabled = true)
      case Some(i: IndirectlyRepositionable) =>
        table.setFooter(NotDirectlyMoveable)
        Axes.foreach(t => t._2.enabled = false)
      case _ =>
    }
    updateCoordinates()
  }

  def updateCoordinates() = {
    target.map { t =>
      val xyz = t.getCurrentPosition.data
      val assign = Axes.map(t => t._2).zip(xyz)
      assign.foreach{ t=>
        t._1.text = t._2.toString
      }
    }
  }

  val Axes = Seq(Axis.X, Axis.Y, Axis.Z).map(axis => (axis, new TextField{columns = 10}))
  val DecAmounts = Seq((Amount.Large, "---"), (Amount.Medium, "--"), (Amount.Small, "-"))
  val IncAmounts = Seq((Amount.Large, "+++"), (Amount.Medium, "++"), (Amount.Small, "+")).reverse

  val NoItem = new Label("No item selected.")
  val NotDirectlyMoveable = new Label("The position of this item can only be set indirectly.")

  val UpdatePositionButton = new Button(new Action("Update position directly") {
    override def apply() = {
      target match {
        case Some(d: DirectlyRepositionable) => Try {
          val v = Axes.map(t => t._2.text.toFloat)
          val point = Point3D(v(0), v(1), v(2))
          d.setCurrentPosition(point)
        }
        case _ => /* do nothing */
      }
    }
  })

  private class Table extends GridBagPanel {
    val lastLine = new BorderPanel {
      layout(NoItem) = BorderPanel.Position.Center
    }

    def setFooter(comp: Component) = {
      lastLine.layout(comp) = BorderPanel.Position.Center
    }

    private def createButton(axis: Axis.Value, spec: (Amount.Value, String), decrease: Boolean): Button = {
      new Button(new Action(spec._2) {
        override def apply() = target.map { t =>
          if (decrease) t.decreaseCurrentCoordinate(axis, spec._1)
          else t.increaseCurrentCoordinate(axis, spec._1)
        }
      })
    }

    // constructor
    {
      var x, y = 0
      for (axis <- Axes) {
        { // axis label
          val constraint = pair2Constraints((x, y))
          constraint.anchor = Anchor.West
          constraint.ipadx = 10
          add(new Label(s"${axis._1.toString}:"), constraint)
          x += 1
        }
        // decrease buttons
        for (b <- DecAmounts) {
          val constraint = pair2Constraints((x, y))
          add(createButton(axis._1, b, decrease = true), constraint)
          x += 1
        }

        { // coordinate textbox
          val constraint = pair2Constraints((x, y))
          constraint.fill = Fill.Horizontal
          add(axis._2, constraint)
          x += 1
        }

        // increase buttons
        for (b <- IncAmounts) {
          val constraint = pair2Constraints((x, y))
          add(createButton(axis._1, b, decrease = false), constraint)
          x += 1
        }
        y += 1
        x = 0
      }
      val constraint = pair2Constraints((x, y))
      constraint.fill = Fill.Horizontal
      constraint.gridwidth = 8
      add(lastLine, constraint)
    }
  }

  private val table = new Table
  layout(table) = BorderPanel.Position.North
}