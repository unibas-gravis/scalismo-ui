/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.view.util

import java.awt
import java.awt.{ CardLayout, Dimension, Insets, Component => AComponent }
import java.util.UUID

import scalismo.ui.view.util.CardPanel.{ ComponentWithUniqueId, CustomCardLayout }

import scala.swing.{ Component, LayoutContainer, Panel }

object CardPanel {

  trait ComponentWithUniqueId extends Component {
    // we don't really care what the ID is, just that it's unique.
    val uniqueId: String = UUID.randomUUID().toString
  }

  class CustomCardLayout extends CardLayout {
    private val minimumWidth = 0
    private val minimumHeight = 0

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
  }

  val NoCard: String = null
}

class CardPanel extends Panel with LayoutContainer {
  type UniqueID = String
  // required for LayoutContainer
  type Constraints = UniqueID

  override lazy val peer = new javax.swing.JPanel(new CustomCardLayout) with SuperMixin

  private val layoutManager = peer.getLayout.asInstanceOf[CustomCardLayout]

  private var cards: Map[UniqueID, ComponentWithUniqueId] = Map.empty
  private var _current: UniqueID = CardPanel.NoCard

  protected def areValid(c: UniqueID): (Boolean, UniqueID) = (true, "")

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

  protected def constraintsFor(comp: Component): UniqueID = cards.iterator.find {
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
