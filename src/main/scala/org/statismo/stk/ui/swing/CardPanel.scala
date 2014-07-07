package org.statismo.stk.ui.swing

import java.awt.CardLayout

import scala.swing.Component
import scala.swing.LayoutContainer
import scala.swing.Panel
import java.util.UUID

// taken from https://issues.scala-lang.org/browse/SI-3933
// and slightly extended

object CardPanel {

  trait CardableComponent extends Component {
    lazy val uniqueId: String = UUID.randomUUID().toString
  }

}

class CardPanel extends Panel with LayoutContainer {
  type Constraints = String
  override lazy val peer = new javax.swing.JPanel(new CardLayout) with SuperMixin

  def layoutManager = peer.getLayout.asInstanceOf[CardLayout]

  private var cards: Map[String, Component] = Map.empty
  private var _current: Constraints = ""

  protected def areValid(c: Constraints) = (true, "")

  def add(cardcomp: CardPanel.CardableComponent): Unit = {
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

  def show(c: CardPanel.CardableComponent): Unit = {
    show(c.uniqueId)
  }

  def show(l: Constraints): Unit = {
    _current = l
    layoutManager.show(peer, l)
  }

  def current: Constraints = _current

  protected def constraintsFor(comp: Component) = cards.iterator.find {
    case (_, c) => c eq comp
  }.map(_._1).orNull
}