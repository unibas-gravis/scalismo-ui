package org.statismo.stk.ui.swing.props


import org.statismo.stk.ui.Repositionable.Amount
import org.statismo.stk.ui._

import scala.swing.GridBagPanel.{Anchor, Fill}
import scala.swing._
import scala.util.Try


class UncertaintyPanel extends BorderPanel with PropertyPanel {
  override val description = "Uncertainty"

  private var target: Option[Uncertainty] = None

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
      case Some(r: Uncertainty) =>
        target = Some(r)
        listenTo(r)
        updateUi()
        true
      case _ => false
    }
  }

  //  reactions += {
  //    case Repositionable.CurrentPositionChanged(t) if target != None && target.get == t => updateCoordinates()
  //  }

  def updateUi() = {
    target match {
      case Some(d: Uncertainty) =>
        Axes.foreach(t => t._2.enabled = true)
      case _ =>
    }
    updateCoordinates()
  }

  def updateCoordinates() = {
    target.map { t =>
      val xyz = List(1,2,3)
      val assign = Axes.map(t => t._2).zip(xyz)
      assign.foreach { t =>
        t._1.text = t._2.toString
      }
    }
  }

  val Axes = Seq(Axis.X, Axis.Y, Axis.Z).map(axis => (axis, new TextField {
    columns = 10
  }))
  val NoItem = new Label("No item selected.")

  private class Table extends GridBagPanel {
    val lastLine = new BorderPanel {
      layout(NoItem) = BorderPanel.Position.Center
    }

    def setFooter(comp: Component) = {
      lastLine.layout(comp) = BorderPanel.Position.Center
    }

    // constructor
    {
      var x, y = 0
      for (axis <- Axes) {
        {
          // axis label
          val constraint = pair2Constraints((x, y))
          constraint.anchor = Anchor.West
          constraint.ipadx = 10
          add(new Label(s"${axis._1.toString}:"), constraint)
          x += 1
        }
        {
          // coordinate textbox
          val constraint = pair2Constraints((x, y))
          constraint.fill = Fill.Horizontal
          add(axis._2, constraint)
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