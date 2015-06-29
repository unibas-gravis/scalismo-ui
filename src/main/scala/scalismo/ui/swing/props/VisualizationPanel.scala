package scalismo.ui.swing.props

import java.awt.{ Dimension => JDimension }
import javax.swing.JComboBox

import scalismo.ui.swing.util.UntypedComboBoxModel
import scalismo.ui.visualization.{ Visualization, VisualizationProvider }
import scalismo.ui.{ SceneTreeObject, Viewport }

import scala.collection.{ immutable, mutable }
import scala.language.existentials
import scala.swing.GridBagPanel.Anchor
import scala.swing._
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

  private[VisualizationPanel] class WorkersPanel extends CombinedPropertiesPanel(description, delegates: _*) with VisualizationsPropertyPanel {
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

  private[VisualizationPanel] class SelectionPanel extends BorderPanel {

    class Entry(viewport: Viewport, alternatives: Seq[Visualization[_]], current: Option[Class[Visualization[_]]]) {
      val inScope = new CheckBox {
        selected = true
        reactions += {
          case x: ButtonClicked => setViewportInScope(viewport, selected)
        }
      }
      val name = new Label(viewport.name)

      val visualization: Component = {
        if (alternatives.length == 0) {
          new Label("--- no visualizations ---")
        } else if (alternatives.length == 1) {
          new Label(alternatives.head.description)
        } else {
          val model = new UntypedComboBoxModel
          alternatives.foreach { entry =>
            model.addElement(entry)
          }
          // WTF? can't directly extend combo, not even like so: (scala 2.10.x):
          // val combo = new JComboBox(model.model) {}
          // ... can't extend, illegal inheritance etc.
          val combo = new JComboBox(model.model)
          Component.wrap(combo)
        }
      }
    }

    class EntryTable extends GridBagPanel {
      // need to redefine because add() is protected in superclass
      def add(comp: Component, position: (Int, Int)) = {
        val const = pair2Constraints(position)
        const.ipadx = 10
        if (position._1 == 0) {
          const.anchor = Anchor.East
        } else {
          const.anchor = Anchor.West
        }
        super.add(comp, const)
      }

      def add(entry: Entry, index: Int): Unit = {
        add(entry.inScope, (0, index))
        add(entry.name, (1, index))
        add(entry.visualization, (2, index))
      }

      def setEntries(entries: immutable.Seq[Entry]): Unit = {
        peer.removeAll()
        val indexed = entries.zipWithIndex
        indexed.foreach {
          case (e, i) =>
            add(e, i)
        }
        SelectionPanel.this.revalidate()
      }
    }

    val entriesTable = new EntryTable

    layout(new BorderPanel {
      layout(entriesTable) = BorderPanel.Position.West
    }) = BorderPanel.Position.Center

    def setTarget(target: Target): Unit = {
      implicit val scene = target.scene
      val entries: immutable.Seq[Entry] = {
        target.scene.viewports.map { viewport =>
          val viewportOption = target.visualizations.get(viewport)
          val currentClassOption: Option[Class[Visualization[_]]] = viewportOption match {
            case (Some(vp)) => Some(vp.getClass.asInstanceOf[Class[Visualization[_]]])
            case _ => None
          }
          val alternatives = target.visualizationFactory.visualizationsFor(viewport)
          new Entry(viewport, alternatives, currentClassOption)
        }.toList
      }
      entriesTable.setEntries(entries)
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
    val tries = vp.scene.viewports.map {
      viewport =>
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
