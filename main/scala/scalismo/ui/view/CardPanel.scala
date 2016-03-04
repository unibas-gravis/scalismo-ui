package scalismo.ui.view

import java.awt
import java.awt.{ CardLayout, Component => AComponent, Dimension, Insets }
import java.util.UUID

import scalismo.ui.view.CardPanel.{ ComponentWithUniqueId, CustomCardLayout }

import scala.swing.{ Component, LayoutContainer, Panel }

object CardPanel {

  trait ComponentWithUniqueId extends Component {
    // we don't really care what the ID is, just that it's unique.
    val uniqueId: String = UUID.randomUUID().toString
  }

  class CustomCardLayout extends CardLayout {
    override def preferredLayoutSize(parent: awt.Container): Dimension = {
      if (activeCards.isDefined) {
        parent.getTreeLock synchronized {
          val sizes = activeCards.get.map(_.getPreferredSize)

          val (maxWidth, maxHeight) = {
            if (sizes.nonEmpty) (sizes.maxBy(_.width).width, sizes.maxBy(_.height).height) else (0, 0)
          }

          val insets: Insets = parent.getInsets
          val preferred = new Dimension(insets.left + insets.right + maxWidth + getHgap * 2, insets.top + insets.bottom + maxHeight + getVgap * 2)
          val dim = new Dimension(Math.max(minimumWidth, preferred.width), Math.max(minimumHeight, preferred.height))
          dim
        }
      } else {
        super.preferredLayoutSize(parent)
      }
    }

    private[CardPanel] var activeCards: Option[List[AComponent]] = None
    var minimumWidth = 0
    var minimumHeight = 0
  }

}

class CardPanel extends Panel with LayoutContainer {
  type UniqueID = String
  // required for LayoutContainer
  type Constraints = UniqueID

  override lazy val peer = new javax.swing.JPanel(new CustomCardLayout) with SuperMixin

  val layoutManager = peer.getLayout.asInstanceOf[CustomCardLayout]

  private var cards: Map[UniqueID, ComponentWithUniqueId] = Map.empty
  private var _current: UniqueID = ""

  protected def areValid(c: UniqueID) = (true, "")

  def add(card: ComponentWithUniqueId): Unit = {
    add(card, card.uniqueId)
  }

  def contains(id: UniqueID): Boolean = {
    cards.contains(id)
  }

  def remove(card: ComponentWithUniqueId): Unit = {
    peer.remove(card.peer)
    cards -= card.uniqueId
  }

  override def add(card: Component, id: UniqueID): Unit = {
    // we need to remove previous components with the same constraints as the new one,
    // otherwise the layout manager loses track of the old one
    cards.get(id).foreach {
      old => cards -= id; peer.remove(old.peer)
    }
    cards += (id -> card.asInstanceOf[ComponentWithUniqueId])
    peer.add(card.peer, id)
  }

  def show(c: ComponentWithUniqueId): Unit = {
    show(c.uniqueId)
  }

  def show(id: UniqueID): Unit = {
    _current = id
    layoutManager.show(peer, id)
  }

  def currentId: UniqueID = _current

  def currentComponent: ComponentWithUniqueId = cards(_current)

  protected def constraintsFor(comp: Component) = cards.iterator.find {
    case (_, c) => c eq comp
  }.map(_._1).orNull

  def setActiveCards(active: List[ComponentWithUniqueId]): Unit = {
    val peers = active.map { c => cards.get(c.uniqueId) }.collect { case opt: Option[Component] if opt.isDefined => opt.get.peer }
    layoutManager.activeCards = Some(peers)
  }

  def unsetActiveCards(): Unit = {
    layoutManager.activeCards = None
  }
}
