package org.statismo.stk.ui.swing

import java.awt
import java.awt.{CardLayout, Dimension, Insets, Component => AComponent}
import java.util.UUID

import org.statismo.stk.ui.swing.CardPanel.{CardableComponent, CustomCardLayout}

import scala.collection.immutable
import scala.swing._

// taken from https://issues.scala-lang.org/browse/SI-3933
// and slightly extended

object CardPanel {

  trait CardableComponent extends Component {
    lazy val uniqueId: String = UUID.randomUUID().toString
  }

  class CustomCardLayout extends CardLayout {
    override def preferredLayoutSize(parent: awt.Container): Dimension = {
      parent.getTreeLock synchronized {
        val all = parent.getComponents.toSeq
        val relevant = considerOnly match {
          case None => all
          case Some(filter) => all.filter(c => filter.contains(c))
        }

        val sizes = relevant.map(_.getPreferredSize)

        val (maxWidth, maxHeight) = {
          if (sizes.nonEmpty) (sizes.maxBy(_.width).width, sizes.maxBy(_.height).height) else (0, 0)
        }

        val insets: Insets = parent.getInsets
        val preferred = new Dimension(insets.left + insets.right + maxWidth + getHgap * 2, insets.top + insets.bottom + maxHeight + getVgap * 2)
        new Dimension(Math.max(minimumWidth, preferred.width), Math.max(minimumHeight, preferred.height))
      }
    }

    private[CardPanel] var considerOnly: Option[immutable.Seq[AComponent]] = None
    var minimumWidth = 0
    var minimumHeight = 0
  }

}

class CardPanel extends Panel with LayoutContainer {
  type Constraints = String
  override lazy val peer = new javax.swing.JPanel(new CustomCardLayout) with SuperMixin

  val layoutManager = peer.getLayout.asInstanceOf[CustomCardLayout]

  private var cards: Map[Constraints, Component] = Map.empty
  private var _current: Constraints = ""

  protected def areValid(c: Constraints) = (true, "")

  def add(cardcomp: CardableComponent): Unit = {
    add(cardcomp, cardcomp.uniqueId)
  }

  def add(comp: Component, l: Constraints): Unit = {
    // we need to remove previous components with the same constraints as the new one,
    // otherwise the layout manager loses track of the old one
    cards.get(l).foreach {
      old => cards -= l; peer.remove(old.peer)
    }
    cards += (l -> comp)
    peer.add(comp.peer, l)
  }

  def show(c: CardableComponent): Unit = {
    show(c.uniqueId)
  }

  private def show(l: Constraints): Unit = {
    _current = l
    layoutManager.show(peer, l)
  }

  def currentId: Constraints = _current

  protected def constraintsFor(comp: Component) = cards.iterator.find {
    case (_, c) => c eq comp
  }.map(_._1).orNull

  def considerOnly(considered: Seq[CardableComponent]) = {
    val jComponents = considered.map { c => cards.get(c.uniqueId)}.filter(_.isDefined).map(o => o.get.peer)
    layoutManager.considerOnly = Some(jComponents.to[immutable.Seq])
  }
}