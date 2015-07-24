package scalismo.ui.swing.props

import java.awt.Dimension
import javax.swing.{ JLabel, JTabbedPane }

import scalismo.ui.swing._
import scalismo.ui.swing.props.SceneObjectPropertiesPanel.{ TabChanged, TabsComponent }
import scalismo.ui.{ EdtPublisher, Workspace }

import scala.swing.BorderPanel.Position._
import scala.swing.event.Event
import scala.swing.{ BorderPanel, Component, Reactor, ScrollPane }

trait PropertyPanel extends CardPanel.CardableComponent {
  def setObject(obj: Option[AnyRef]): Boolean

  def description: String

  private[props] var workspace: Option[Workspace] = None

  override def toString(): String = description

  override def revalidate() = {
    super.revalidate()
    if (preferredSize.width > size.width) {
      // this is a hack ...
      workspace.foreach { _.publishPleaseLayoutAgain() }
    }
  }
}

object SceneObjectPropertiesPanel extends EdtPublisher {
  private val appearance = new CombinedPropertiesPanel("Appearance", new ColorPropertyPanel, new ScalarRangePropertyPanel, new OpacityPropertyPanel, new LineWidthPropertyPanel, new Radius1DPropertyPanel)
  val DefaultViewProviders: Seq[PropertyPanel] = Seq(new SlicingPositionPanel, new ImageWindowLevelPanel, new PrincipalComponentsPanel, appearance, new RepositionableControlPanel, new UncertaintyPanel)

  class TabsComponent extends Component with EdtPublisher {
    outer =>
    override lazy val peer: JTabbedPane = new JTabbedPane() {
      override def setSelectedIndex(index: Int) = {
        super.setSelectedIndex(index)
        if (getTabCount > index) {
          val tab = getTabComponentAt(index).asInstanceOf[TabComponent]
          if (tab != null) {
            outer.publishEdt(TabChanged(outer, tab.view))
          }
        }
      }
    }

    def add(view: PropertyPanel) = {
      val index = peer.getTabCount
      peer.addTab(null, null)
      peer.setTabComponentAt(index, new TabComponent(view))
    }

    def setSelectedItem(view: PropertyPanel): Boolean = {
      for (index <- 0 to peer.getTabCount) {
        val tv = peer.getTabComponentAt(index).asInstanceOf[TabComponent].view
        if (tv.uniqueId == view.uniqueId) {
          peer.setSelectedIndex(index)
          return true
        }
      }
      false
    }

  }

  class TabComponent(val view: PropertyPanel) extends JLabel(view.description)

  case class TabChanged(source: TabsComponent, selected: PropertyPanel) extends Event

}

class SceneObjectPropertiesPanel(val workspace: Workspace) extends BorderPanel with Reactor {
  lazy val availableProviders = SceneObjectPropertiesPanel.DefaultViewProviders

  private val emptyPanel = new BorderPanel with CardPanel.CardableComponent {
    val zero = new Dimension(0, 0)
    peer.setPreferredSize(zero)
    peer.setMinimumSize(zero)
    peer.setMaximumSize(zero)
    peer.setSize(zero)
  }

  lazy val cards = new CardPanel {
    add(emptyPanel, emptyPanel.uniqueId)
  }

  availableProviders.foreach { p =>
    cards.add(p)
    p.workspace = Some(workspace)
  }
  cards.peer.revalidate()
  // initialize once
  cards.layoutManager.minimumWidth = cards.peer.getPreferredSize.width

  val scroll = new ScrollPane() {
    contents = cards
    enabled = false

    horizontalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Always
  }

  layout(scroll) = Center

  val tabs = new TabsComponent
  layout(tabs) = North

  listenTo(workspace)
  listenTo(tabs)
  updateListAndContent()

  reactions += {
    case Workspace.SelectedObjectChanged(ws) if ws eq workspace => updateListAndContent()
    case TabChanged(t, v) if t == tabs && tabs.enabled => updateContent(Some(v))
  }

  def updateListAndContent(): Unit = {
    // side effect: some events might still fire, but they
    // are ignored because the tabs are not enabled (see reactions above)
    tabs.enabled = false
    val currentObject = workspace.selectedObject

    tabs.peer.removeAll()
    val applicable = availableProviders.filter(_.setObject(currentObject))
    cards.considerOnly(applicable)
    applicable foreach (view => tabs.add(view))

    // if cards.current also applies to the newly selected object,
    // keep it showing (and update the selected tab accordingly)
    val alreadyShowing = applicable.find(_.uniqueId == cards.currentId)
    alreadyShowing match {
      case Some(view) => tabs.setSelectedItem(view)
      case None => updateContent(applicable.headOption)
    }
    tabs.enabled = applicable.nonEmpty
  }

  def updateContent(viewOption: Option[PropertyPanel]): Unit = {
    val (enableScroll, display) = viewOption match {
      case Some(view) => (true, view)
      case None => (false, emptyPanel)
    }

    scroll.enabled = enableScroll

    cards.show(display)
    scroll.peer.getVerticalScrollBar.setValue(0)
    val updateLayout = cards.preferredSize.width > cards.size.width
    if (updateLayout) workspace.publishPleaseLayoutAgain()
  }
}
