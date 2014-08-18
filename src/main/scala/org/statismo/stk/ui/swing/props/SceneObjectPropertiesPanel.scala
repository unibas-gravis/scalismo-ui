package org.statismo.stk.ui.swing.props

import java.awt.Dimension
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.North
import scala.swing.Component
import scala.swing.Reactor
import scala.swing.ScrollPane
import scala.swing.Swing
import org.statismo.stk.ui.EdtPublisher
import org.statismo.stk.ui.Workspace
import javax.swing.JComboBox
import scala.language.existentials
import scala.language.reflectiveCalls
import org.statismo.stk.ui.swing.util.UntypedComboBoxModel
import org.statismo.stk.ui.swing._
import scala.swing.event.SelectionChanged

trait PropertyPanel extends CardPanel.CardableComponent {
  def setObject(obj: Option[AnyRef]): Boolean

  def description: String

  private[props] var workspace: Option[Workspace] = None

  override def toString(): String = {
    description
  }

  override def revalidate() = {
    super.revalidate()
    if (preferredSize.width > size.width) {
      // this is a hack ...
      workspace.map {
        _.publishPleaseLayoutAgain()
      }
    }
  }
}

object SceneObjectPropertiesPanel extends EdtPublisher {
  private val appearance = new VisualizationPanel("Appearance", new RadiusPanel, new LineThicknessPanel, new ColorablePanel)
  val DefaultViewProviders: Seq[PropertyPanel] = Seq(new SlicingPositionPanel, new PrincipalComponentsPanel, appearance, new RepositionableControlPanel)
}

class SceneObjectPropertiesPanel(val workspace: Workspace) extends BorderPanel with Reactor {
  lazy val availableProviders = SceneObjectPropertiesPanel.DefaultViewProviders
  private val applicableViews = new UntypedComboBoxModel

  private val emptyPanel = new BorderPanel with CardPanel.CardableComponent {
    peer.setPreferredSize(new Dimension(1, 1))
  }

  lazy val cards = new CardPanel {
    add(emptyPanel, emptyPanel.uniqueId)
    availableProviders.foreach {
      p =>
        add(p)
        p.workspace = Some(workspace)
    }
  }

  lazy val combo = new Component with EdtPublisher {
    override lazy val peer = new JComboBox(applicableViews.model)
    peer.addActionListener(Swing.ActionListener {
      e =>
        publishEdt(SelectionChanged(this))
    })
  }
  layout(combo) = North

  listenTo(workspace)
  listenTo(combo)
  updateListAndContent()

  reactions += {
    case SelectionChanged(e) => updateContent()
    case Workspace.SelectedObjectChanged(ws) if ws eq workspace =>
      updateListAndContent()
  }

  def updateListAndContent() {
    val currentObject = workspace.selectedObject
    val applicable = availableProviders.filter(_.setObject(currentObject))
    applicableViews.model.removeAllElements()
    applicable foreach {
      v =>
        applicableViews.addElement(v)
    }
    applicable.foreach {
      p =>
        if (cards.current == p.uniqueId) {
          applicableViews.model.setSelectedItem(p)
        }
    }
    combo.enabled = !applicable.isEmpty
    updateContent()
  }

  val scroll = new ScrollPane() {
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Always
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Always
    contents = cards
  }

  layout(scroll) = Center

  def updateContent() {
    val view = applicableViews.model.getSelectedItem.asInstanceOf[PropertyPanel]
    if (view != null) {
      if (cards.current != view.uniqueId) {
        cards.show(view)
        // this is a hack...
        if (cards.preferredSize.width > cards.size.width) {
          workspace.publishPleaseLayoutAgain()
        }
      }
    } else {
      cards.show(emptyPanel)
    }
  }
}