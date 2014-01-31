package org.statismo.stk.ui.view.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import scala.swing.Button
import scala.swing.Reactor
import org.statismo.stk.ui.Workspace
import scala.swing.Publisher
import scala.swing.event.Event
import scala.swing.ListView
import javax.swing.JComboBox
import scala.swing.Component
import javax.swing.ComboBoxModel
import javax.swing.DefaultComboBoxModel
import org.statismo.stk.ui.SceneObject
import scala.swing.ComboBox
import scala.swing.Swing
import scala.swing.event.SelectionChanged
import scala.swing.event.SelectionChanged
import scala.swing.ScrollPane
import scala.swing.Label

trait SceneObjectPropertyView extends Component {
  def setObject(obj: Option[SceneObject]): Boolean
  def description: String
  override def toString(): String = { description }
}

object SceneObjectPropertiesPanel extends Publisher {
  case object AvailableViewsChanged extends Event
  private var _availableViews: Seq[SceneObjectPropertyView] = Seq(new PrincipalComponentsPanel(), new AppearancePanel())
  def availableViews = { _availableViews }
  def availableViews_=(newViews: Seq[SceneObjectPropertyView]) = {
    if (_availableViews != newViews) {
      _availableViews = newViews
      publish(AvailableViewsChanged)
    }
  }
}

class SceneObjectPropertiesPanel(val workspace: Workspace) extends BorderPanel with Reactor {
  import SceneObjectPropertiesPanel.availableViews
  val applicableViews = new DefaultComboBoxModel[SceneObjectPropertyView]
  val combo = new Component {
    override lazy val peer = new JComboBox(applicableViews) {
    }
    peer.addActionListener(Swing.ActionListener { e =>
      publish(SelectionChanged(this))
    })
  }
  layout(combo) = North

  listenTo(workspace)
  listenTo(combo)
  listenTo(SceneObjectPropertiesPanel)
  updateListAndContent()

  reactions += {
    case SelectionChanged(e) => updateContent
    case Workspace.SelectedObjectChanged => updateListAndContent
    case SceneObjectPropertiesPanel.AvailableViewsChanged => updateListAndContent
  }

  def updateListAndContent() {
    val currentObject = workspace.selectedObject
    val applicable = availableViews.filter(_.setObject(currentObject))
    applicableViews.removeAllElements()
    applicable foreach ({ v =>
      applicableViews.addElement(v)
      if (scroll.contents == v) {
        applicableViews.setSelectedItem(v)
      }
    })
    updateContent
  }

  val scroll = new ScrollPane() {
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Always
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Always
  }
  layout(scroll) = Center
  
  def updateContent() {
	val view = applicableViews.getSelectedItem().asInstanceOf[SceneObjectPropertyView]
//	val show = if (view != null) {
//	  layout(new ScrollPane(view)) = Center
//	} else {
//	  new Label("...empty...")
//	}
//	//layout(show) = Center
	if (view != null && view != scroll.contents) {
	  scroll.contents = view
	}
  }
}