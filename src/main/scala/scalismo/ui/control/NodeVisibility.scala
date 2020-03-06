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

package scalismo.ui.control

import scalismo.ui.control.NodeVisibility._
import scalismo.ui.event.{Event, ScalismoPublisher}
import scalismo.ui.model.SceneNode
import scalismo.ui.model.capabilities.RenderableSceneNode
import scalismo.ui.view._
import scalismo.ui.view.perspective.Perspective

import scala.collection.mutable
//import scala.language.implicitConversions

/**
 * This class controls the visibility of nodes in the various viewports of a frame.
 *
 */
object NodeVisibility {

  // convenience type aliases
  type Node = RenderableSceneNode
  type Context = ViewportPanel

  sealed trait State {}

  case object Visible extends State

  case object Invisible extends State

  case object PartlyVisible extends State

  object event {

    case class NodeVisibilityChanged(node: SceneNode, viewport: ViewportPanel) extends Event

  }

}

class NodeVisibility(frame: ScalismoFrame) extends ScalismoPublisher {
  private val hiddenMap = new mutable.WeakHashMap[Node, Set[Context]]

  // lowest level: single node, non-empty set of contexts
  private def getStateInMap(node: Node, contexts: Set[Context]): State = {
    hiddenMap.get(node) match {
      case None => Visible
      case Some(hiddens) =>
        val intersection = hiddens.intersect(contexts)
        if (intersection.isEmpty) {
          Visible
        } else if (intersection == contexts) {
          Invisible
        } else {
          PartlyVisible
        }
    }
  }

  // slightly higher level: set of nodes, set of contexts
  private def getStateInMap(nodes: Set[Node], contexts: Set[Context]): State = {
    require(nodes.nonEmpty && contexts.nonEmpty)
    val nodeStates = nodes.map(node => getStateInMap(node, contexts))

    // Now we have a set of states. If the set contains a single value, then that's
    // our result. Otherwise, it is necessarily a mixed/partial visibility.
    if (nodeStates.size == 1) {
      nodeStates.toList.head
    } else {
      PartlyVisible
    }
  }

  // public API
  def getVisibilityState(nodes: Iterable[Node], viewports: Iterable[Context]): State = {
    val nodesSet = nodes.toSet
    val viewportsSet = viewports.toSet
    // there's no correct answer to "is nothing visible in nothing?" or similar
    require(nodesSet.nonEmpty && viewportsSet.nonEmpty)

    getStateInMap(nodesSet, viewportsSet)
  }

  // convenience public API
  def getVisibilityState(node: Node, viewports: Iterable[Context]): State = {
    getVisibilityState(List(node), viewports)

  }

  def getVisibilityState(nodes: Iterable[Node], viewport: Context): State = {
    getVisibilityState(nodes, List(viewport))
  }

  def getVisibilityState(node: Node, viewport: Context): State = {
    getVisibilityState(List(node), List(viewport))
  }

  def isVisible(node: RenderableSceneNode, viewport: ViewportPanel): Boolean = {
    getVisibilityState(node, viewport) == Visible
  }

  // lowest level: single node, non-empty set of contexts
  private def setStateInMap(node: Node, contexts: Set[Context], hide: Boolean): Unit = {
    hiddenMap.get(node) match {
      case None =>
        // No previous state; if we want to hide a node, we just add the contexts.
        // If we want the node to be shown, there's nothing to do anyway, because
        // absence of the node in the map signifies that it's visible.
        if (hide) {
          hiddenMap(node) = contexts
        }
      case Some(hiddens) =>
        if (hide) {
          hiddenMap(node) = hiddens union contexts
        } else {
          val resulting = hiddens diff contexts
          if (resulting.nonEmpty) {
            hiddenMap(node) = resulting
          } else {
            hiddenMap.remove(node)
          }
        }
    }
    // We publish the event unconditionally, even if nothing has actually changed.
    // This could be optimized in the future, if needed.
    contexts.foreach { viewport =>
      publishEvent(NodeVisibility.event.NodeVisibilityChanged(node, viewport))
    }
  }

  private def setStateInMap(nodes: Set[Node], contexts: Set[Context], hide: Boolean): Unit = {
    nodes.foreach { node =>
      setStateInMap(node, contexts, hide)
    }
  }

  // public API
  def setVisibility(nodes: Iterable[Node], viewports: Iterable[Context], show: Boolean): Unit = {
    val nodesSet = nodes.toSet
    val viewportsSet = viewports.toSet

    if (nodesSet.nonEmpty && viewportsSet.nonEmpty) {
      setStateInMap(nodesSet, viewportsSet, !show)
    }
  }

  // convenience public API

  def setVisibility(node: Node, viewports: Iterable[Context], show: Boolean): Unit = {
    setVisibility(List(node), viewports, show)
  }

  def setVisibility(nodes: Iterable[Node], viewport: Context, show: Boolean): Unit = {
    setVisibility(nodes, List(viewport), show)
  }

  def setVisibility(node: Node, viewport: Context, show: Boolean): Unit = {
    setVisibility(List(node), List(viewport), show)
  }

  private def handlePerspectiveChange(current: Perspective, previous: Perspective): Unit = {
    // We keep it simple here, and only hide items if they have been completely hidden before.
    // anything else would just be guesswork.

    val oldViewports = previous.viewports
    val completelyHidden = hiddenMap.keys.collect { case n if getVisibilityState(n, oldViewports) == Invisible => n }
    hiddenMap.clear()
    setVisibility(completelyHidden, current.viewports, show = false)
  }

  def initialize(): Unit = {
    listenTo(frame.perspective)
  }

  reactions += {
    case PerspectivePanel.event.PerspectiveChanged(_, current, previous) if previous.isDefined =>
      handlePerspectiveChange(current, previous.get)
  }
}
