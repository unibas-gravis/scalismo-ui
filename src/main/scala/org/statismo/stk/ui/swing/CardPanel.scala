package org.statismo.stk.ui.swing

import scala.swing.LayoutContainer
import scala.swing.Panel
import java.awt.CardLayout
import scala.swing.Component

// taken from https://issues.scala-lang.org/browse/SI-3933
// and slightly extended
class CardPanel extends Panel with LayoutContainer {
  type Constraints = String
  def layoutManager = peer.getLayout.asInstanceOf[CardLayout]
  override lazy val peer = new javax.swing.JPanel(new CardLayout) with SuperMixin

  private var cards: Map[String, Component] = Map.empty
  private var _current: Constraints = ""

  protected def areValid(c: Constraints) = (true, "")
  protected def add(comp: Component, l: Constraints) = {
    // we need to remove previous components with the same constraints as the new one,
    // otherwise the layout manager loses track of the old one
    cards.get(l).foreach { old => cards -= l; peer.remove(old.peer) }
    cards += (l -> comp)
    peer.add(comp.peer, l)
  }

  def show(l: Constraints) = {
    _current = l
    layoutManager.show(peer, l)
  }
  
  def current: Constraints = _current

  protected def constraintsFor(comp: Component) = cards.iterator.find { case (_, c) => c eq comp }.map(_._1).orNull
}