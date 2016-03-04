package scalismo.ui.view

import java.awt.Dimension
import javax.swing.{ JLabel, JTabbedPane }

import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.view.NodePropertiesPanel.Tabs
import scalismo.ui.view.NodePropertiesPanel.Tabs.event.TabChanged
import scalismo.ui.view.properties.{ ColorPropertyPanel, CombinedPropertiesPanel, OpacityPropertyPanel, PropertyPanel }

import scala.swing.event.Event
import scala.swing.{ BorderPanel, Component, ScrollPane }

object NodePropertiesPanel {

  class Tab(val view: PropertyPanel) extends JLabel(view.description)

  class Tabs extends Component with ScalismoPublisher {
    outer =>
    override lazy val peer: JTabbedPane = new JTabbedPane() with SuperMixin {
      override def setSelectedIndex(index: Int) = {
        super.setSelectedIndex(index)
        if (getTabCount > index) {
          getTabComponentAt(index) match {
            case tab: Tab => outer.publishEvent(TabChanged(outer, tab))
            case _ => // do nothing
          }
        }
      }

      private var oneTabHeight = -1
      private var emptyHeight = -1

      override def getPreferredSize: Dimension = {
        // this method currently seems to be broken in the superclass. The more tabs there are, the more
        // empty space it adds below the actual tabs. The original version adds *lots* of empty space. This
        // version unexplicably still exposes a similar behavior, but on a much smaller scale (a few pixels,
        // not hundreds).
        val dim = super.getPreferredSize
        if (oneTabHeight >= 0 && emptyHeight >= 0) {
          val rows = getTabRunCount
          val newHeight = emptyHeight + (rows * (oneTabHeight - emptyHeight))
          dim.height = newHeight
        }
        dim
      }

      override def addTab(name: String, component: java.awt.Component): Unit = {
        if (getTabCount == 0) {
          emptyHeight = super.getPreferredSize.height
          super.addTab(name, component)
          oneTabHeight = super.getPreferredSize.height
        } else super.addTab(name, component)
      }
    }

    def removeAll(): Unit = {
      peer.removeAll()
    }

    def add(view: PropertyPanel): Unit = {
      val index = peer.getTabCount
      peer.addTab(view.description, null)
      peer.setTabComponentAt(index, new Tab(view))
    }

    def setSelectedItem(view: PropertyPanel): Boolean = {
      (0 until peer.getTabCount).foreach { index =>
        peer.getTabComponentAt(index) match {
          case tab: Tab if tab.view.uniqueId == view.uniqueId =>
            peer.setSelectedIndex(index)
            return true
          case _ => // nothing
        }
      }
      false
    }

  }

  object Tabs {

    object event {

      case class TabChanged(source: Tabs, selected: Tab) extends Event

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
    // default implementation: instantiate all builtin providers.
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

  val tabs = new Tabs

  // actual content.
  val cards = new CardPanel {
    add(empty)
  }

  val scroll = new ScrollPane() {
    contents = cards
    enabled = false

    horizontalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
    verticalScrollBarPolicy = ScrollPane.BarPolicy.Always
  }

  def updateTabsAndCard(): Unit = {
    // side effect: some events might still fire, but they
    // are ignored because the tabs are not enabled (see reactions)
    tabs.enabled = false

    val selected = frame.selectedNodes

    tabs.peer.removeAll()
    val active = providers.filter(_.setNodes(selected))
    cards.setActiveCards(active)
    active foreach (view => tabs.add(view))

    // if cards.current (i.e., what was showing before)
    // also applies to the newly selected object,
    // keep showing it, and set it again as the selected tab.
    // otherwise, set the content to the first active view
    active.find(_.uniqueId == cards.currentId) match {
      case Some(view) => tabs.setSelectedItem(view)
      case None => updateCard(active.headOption)
    }
    tabs.enabled = active.nonEmpty
  }

  def updateCard(panelToShow: Option[PropertyPanel]): Unit = {
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

  // constructor

  reactions += {
    case ScalismoFrame.event.SelectedNodesChanged(_) => updateTabsAndCard()
    case TabChanged(_, tab) if tabs.enabled => updateCard(Some(tab.view))
  }

  layout(tabs) = BorderPanel.Position.North
  layout(scroll) = BorderPanel.Position.Center

  setupProviders()

  listenTo(frame)
  listenTo(tabs)

  updateTabsAndCard()

}
