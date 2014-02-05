package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import scala.swing.Reactor
import org.statismo.stk.ui.Workspace
import scala.swing.Publisher
import scala.swing.event.Event
import scala.swing.Component
import javax.swing.DefaultComboBoxModel
import scala.swing.Swing
import scala.swing.event.SelectionChanged
import scala.swing.event.SelectionChanged
import scala.swing.ScrollPane
import javax.swing.JComboBox
import org.statismo.stk.ui.SceneTreeObject
import scala.swing.LayoutContainer
import scala.swing.Panel
import java.util.UUID
import java.awt.Dimension

trait SceneObjectPropertyPanel extends Component {
  def setObject(obj: Option[SceneTreeObject]): Boolean
  def description: String
  override def toString(): String = { description }

  lazy val uniqueId = UUID.randomUUID().toString()
}

object SceneObjectPropertiesPanel extends Publisher {
  val DefaultViewProviders: Seq[SceneObjectPropertyPanel] = Seq(new PrincipalComponentsPanel, new ColorablePanel, new NameablePanel)
}

class SceneObjectPropertiesPanel(val workspace: Workspace) extends BorderPanel with Reactor {
  lazy val availableProviders = SceneObjectPropertiesPanel.DefaultViewProviders
  private val applicableViews = new DefaultComboBoxModel[SceneObjectPropertyPanel]

  private val emptyPanel = new Panel {
    lazy val uniqueId = UUID.randomUUID().toString()
    peer.setPreferredSize(new Dimension(1,1))
  }

  lazy val cards = new CardPanel {
    add(emptyPanel, emptyPanel.uniqueId)
    availableProviders.foreach { p =>
      add(p, p.uniqueId)
    }
  }

  lazy val combo = new Component {
    override lazy val peer = new JComboBox(applicableViews) {
    }
    peer.addActionListener(Swing.ActionListener { e =>
      publish(SelectionChanged(this))
    })
  }
  layout(combo) = North

  listenTo(workspace)
  listenTo(combo)
  updateListAndContent()

  reactions += {
    case SelectionChanged(e) => updateContent
    case Workspace.SelectedObjectChanged => updateListAndContent
  }

  def updateListAndContent() {
    val currentObject = workspace.selectedObject
    val applicable = availableProviders.filter(_.setObject(currentObject))
    applicableViews.removeAllElements()
    applicable foreach ({ v =>
      applicableViews.addElement(v)
    })
    applicable.foreach { p =>
      if (cards.current == p.uniqueId) {
        applicableViews.setSelectedItem(p)
      }
    }
    updateContent
  }

  val scroll = new ScrollPane() {
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Always
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Always
    contents = cards
  }

  layout(scroll) = Center

  def updateContent() {
    val view = applicableViews.getSelectedItem().asInstanceOf[SceneObjectPropertyPanel]
    if (view != null) {
      if (cards.current != view.uniqueId) {
        cards.show(view.uniqueId)
      }
    } else {
        cards.show(emptyPanel.uniqueId)
    }
  }
}