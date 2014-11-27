package org.statismo.stk.ui.swing.props

import java.awt.Dimension
import javax.swing.JComboBox

import org.statismo.stk.ui.{EdtPublisher, Workspace}
import org.statismo.stk.ui.swing._
import org.statismo.stk.ui.swing.util.UntypedComboBoxModel

import scala.language.{existentials, reflectiveCalls}
import scala.swing.BorderPanel.Position.{Center, North}
import scala.swing.{BorderPanel, Component, Reactor, ScrollPane, Swing}
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

  val scroll = new ScrollPane() {
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Always
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Always
    contents = cards
  }

  layout(scroll) = Center

  listenTo(workspace)
  listenTo(combo)
  updateListAndContent()

  reactions += {
    case Workspace.SelectedObjectChanged(ws) if ws eq workspace => updateListAndContent()
    case SelectionChanged(e) if e == combo && combo.enabled => updateContent()
  }

  def updateListAndContent() {
    // side effect: the SelectionChanged event still fires on addElement, setSelectedItem,
    // but is ignored because the combo is not enabled (see reactions above)
    combo.enabled = false
    val currentObject = workspace.selectedObject

    applicableViews.model.removeAllElements()
    val applicable = availableProviders.filter(_.setObject(currentObject))
    applicable foreach applicableViews.addElement

    // if cards.current also applies to the newly selected object,
    // keep it showing (and update the combobox accordingly)
    val alreadyShowing = applicable.find(_.uniqueId == cards.current)
    alreadyShowing match {
      case Some(view) => applicableViews.model.setSelectedItem(view)
      case None => updateContent()
    }
    combo.enabled = applicable.nonEmpty
  }

  def updateContent(): Unit = {
    val view = applicableViews.model.getSelectedItem.asInstanceOf[PropertyPanel]
    if (view != null) {
      if (cards.current != view.uniqueId) {
        cards.show(view)
        // scroll to top of view
        scroll.peer.getVerticalScrollBar.setValue(0)
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