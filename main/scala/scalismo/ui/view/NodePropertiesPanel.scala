package scalismo.ui.view

import java.awt.Dimension
import javax.swing.{ JLabel, JTabbedPane }

import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.view.NodePropertiesPanel.TabsComponent
import scalismo.ui.view.NodePropertiesPanel.TabsComponent.event.TabChanged
import scalismo.ui.view.properties.{ ColorPropertyPanel, CombinedPropertiesPanel, OpacityPropertyPanel, PropertyPanel }

import scala.swing.event.Event
import scala.swing.{ BorderPanel, Component, ScrollPane }

object NodePropertiesPanel {

  class TabComponent(val view: PropertyPanel) extends JLabel(view.description)

  class TabsComponent extends Component with ScalismoPublisher {
    outer =>
    override lazy val peer: JTabbedPane = new JTabbedPane() {
      override def setSelectedIndex(index: Int) = {
        super.setSelectedIndex(index)
        if (getTabCount > index) {
          getTabComponentAt(index) match {
            case tab: TabComponent => outer.publishEdt(TabChanged(outer, tab))
            case _ => // do nothing
          }
        }
      }
    }

    def removeAll(): Unit = {
      peer.removeAll()
    }

    def add(view: PropertyPanel): Unit = {
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

  object TabsComponent {

    object event {

      case class TabChanged(source: TabsComponent, selected: TabComponent) extends Event

    }

  }

  // built-in, default providers. These are defined here as functions which construct the object,
  // so they're only instantiated at runtime.
  val BuiltinProviders: List[ScalismoFrame => PropertyPanel] = {
    def appearancePanel = { f: ScalismoFrame => new CombinedPropertiesPanel(f, "Appearance", new ColorPropertyPanel(f), new OpacityPropertyPanel(f)) }
    List(
      appearancePanel,
      // I know this is redundant. Will be removed once we have more implementations.
      { f => new ColorPropertyPanel(f) }, { f => new OpacityPropertyPanel(f) }
    )
  }

}

class NodePropertiesPanel(frame: ScalismoFrame) extends BorderPanel {

  private var _providers: List[PropertyPanel] = Nil

  // "providers" are PropertyPanels that can show various properties (like color etc).
  // If the default implementation has to be modified, developers can either
  // call the respective methods to add/remove providers, or override the
  // setupProviders() method altogether.
  def providers: List[PropertyPanel] = _providers

  def addProvider(provider: PropertyPanel) = {
    _providers ++= List(provider)
    cards.add(provider)
  }

  def removeProvider(provider: PropertyPanel) = {
    _providers = _providers.filterNot(_ eq provider)
    cards.remove(provider)
  }

  def setupProviders() = {
    NodePropertiesPanel.BuiltinProviders.foreach { constructor =>
      addProvider(constructor(frame))
    }
  }

  // placeholder object that gets shown when nothing is to be shown :-)
  val empty = new BorderPanel with CardPanel.ComponentWithUniqueId {
    val zero = new Dimension(0, 0)
    peer.setPreferredSize(zero)
    peer.setMinimumSize(zero)
    peer.setMaximumSize(zero)
    peer.setSize(zero)
  }

  val tabs = new TabsComponent

  lazy val cards = new CardPanel {
    add(empty)
  }

  val scroll = new ScrollPane() {
    contents = cards
    enabled = false

    horizontalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Always
  }

  def updateListAndContent(): Unit = {
    // side effect: some events might still fire, but they
    // are ignored because the tabs are not enabled (see reactions)
    tabs.enabled = false

    val selected = frame.selectedNodes

    tabs.peer.removeAll()
    val active = providers.filter(_.setNodes(selected))
    cards.setActiveCards(active)
    active foreach (view => tabs.add(view))

    // if cards.current also applies to the newly selected object,
    // keep it showing (and update the selected tab accordingly)
    val alreadyShowing = active.find(_.uniqueId == cards.currentId)

    alreadyShowing match {
      case Some(view) => tabs.setSelectedItem(view)
      case None => updateContent(active.headOption)
    }
    tabs.enabled = active.nonEmpty
  }

  def updateContent(panelToShow: Option[PropertyPanel]): Unit = {
    val (enableScroll, card) = panelToShow match {
      case Some(view) => (true, view)
      case None => (false, empty)
    }

    scroll.enabled = enableScroll

    cards.show(card)
    // scroll up
    scroll.peer.getVerticalScrollBar.setValue(0)

    if (size.width < preferredSize.width) {
      // we're too small!
      frame.peer.revalidate()
    }
  }

  layout(tabs) = BorderPanel.Position.North
  layout(scroll) = BorderPanel.Position.Center

  setupProviders()

  listenTo(frame)
  listenTo(tabs)
  updateListAndContent()

  reactions += {
    case ScalismoFrame.event.SelectedNodesChanged(_) => updateListAndContent()
    case TabChanged(_, tab) if tabs.enabled => updateContent(Some(tab.view))
  }
}
