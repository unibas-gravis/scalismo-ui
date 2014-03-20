package org.statismo.stk.ui.swing.props

import org.statismo.stk.ui.visualization.{Visualization, VisualizationProvider}
import org.statismo.stk.ui.{Viewport, SceneTreeObject}
import scala.collection.{mutable, immutable}
import scala.swing._
import scala.Some
import scala.swing.GridBagPanel.Anchor
import scala.swing.event.ButtonClicked
import scala.util.Failure

trait VisualizationsPropertyPanel extends PropertyPanel {
  def setVisualizations(visualizations: immutable.Seq[Visualization[_]]): Boolean
  override final def setObject(obj: Option[AnyRef]): Boolean = {
    throw new UnsupportedOperationException
  }
}

class VisualizationPanel(override val description: String, delegates: VisualizationsPropertyPanel*) extends BorderPanel with PropertyPanel {
  type Target = SceneTreeObject with VisualizationProvider[_]

  private [VisualizationPanel] class WorkersPanel extends CombinedPropertiesPanel(description, delegates:_*) with VisualizationsPropertyPanel {
    override def setVisualizations(visualizations: immutable.Seq[Visualization[_]]): Boolean = {
      val ok = delegates.map(d => delegatedSetVisualizations(d, visualizations)).foldLeft(false)({
        (x, y) => x || y
      })
      revalidate()
      ok
    }

    def delegatedSetVisualizations(del: VisualizationsPropertyPanel, visualizations: immutable.Seq[Visualization[_]]): Boolean = {
      val ok = del.setVisualizations(visualizations)
      del.visible = ok
      ok
    }
  }

  private [VisualizationPanel] class SelectionPanel extends BorderPanel {
    class Entry(viewport: Viewport) {
      val checkbox = new CheckBox{
        selected = true
        reactions += {
          case x: ButtonClicked => setViewportInScope(viewport, selected)
        }
      }
      val name = new Label(viewport.name)
      val fixme = new Label("visSelect (FIXME)")
    }

    class EntryTable extends GridBagPanel {
      // need to redefine because add() is protected in superclass
      def add(comp: Component, position: (Int, Int)) = {
        val const = pair2Constraints(position)
        const.ipadx = 10
        if (position._1 == 0) {
          const.anchor = Anchor.East
        } else if (position._1 == 2) {
          const.anchor = Anchor.West
        }
        super.add(comp, const)
      }

      def add(entry: Entry, index: Int): Unit = {
        add(entry.checkbox, (0, index))
        add(entry.name, (1, index))
        add(entry.fixme, (2, index))
      }

      def setEntries(entries: immutable.Seq[Entry]) : Unit = {
        peer.removeAll()
        val indexed = entries.zipWithIndex
        indexed.foreach{ case (e,i)=>
          add(e,i)
        }
        SelectionPanel.this.revalidate()
      }
    }

    val table = new EntryTable
    layout(new BorderPanel {
      layout(table) = BorderPanel.Position.West
    }) = BorderPanel.Position.Center

    def setTarget(target: Target): Unit = {
      val entries: immutable.Seq[Entry] = {
        val viewports = target.scene.viewports.map {viewport =>
          //val visTries = target.scene.visualizations.tryGet(target, viewport)
          viewport
        }
        viewports.map(vp => new Entry(vp)).toList
      }
      table.setEntries(entries)
    }
  }

  val workers = new WorkersPanel
  val selection = new SelectionPanel

  layout(workers) = BorderPanel.Position.Center
  layout(selection) = BorderPanel.Position.North

  private var scope: Target = null
  override def setObject(obj: Option[AnyRef]): Boolean = {
    outOfScope.clear()
    obj match {
      case None => false
      case Some(vp: Target) =>
        val vis: immutable.Seq[Visualization[_]] = visualizationsInScope(vp)
        if (vis.isEmpty) false
        else {
          scope = vp
          selection.setTarget(scope)
          val ok = workers.setVisualizations(vis)
          revalidate()
          ok
        }
      case _ => false
    }
  }

  def setViewportInScope(viewport: Viewport, inScope: Boolean) = {
    outOfScope(viewport) = !inScope
    workers.setVisualizations(visualizationsInScope(scope))
  }

  def visualizationsInScope(vp: Target): immutable.Seq[Visualization[_]] = {
    val tries = vp.scene.viewports.map {viewport =>
      if (outOfScope.getOrElse(viewport, false)) {
        Failure(new IllegalStateException("out of scope"))
      } else {
        vp.scene.visualizations.tryGet(vp, viewport)
      }
    }
    tries.filter(_.isSuccess).map(_.get).toList
  }

  private val outOfScope = new mutable.WeakHashMap[Viewport, Boolean]
}
