package scalismo.ui.control

import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.SceneNode
import scalismo.ui.view._
import scalismo.ui.view.perspective.Perspective

import scala.collection.mutable

object NodeVisibility {

  import scala.language.implicitConversions

  object Visible {
    implicit def visibleAsBoolean(v: Visible): Boolean = v.visible
  }

  class Visible private[NodeVisibility] (map: NodeVisibility, node: SceneNode) {
    def visible: Boolean = map.isVisible(node)

    def visible_=(show: Boolean): Unit = map.setVisible(node, map.allViewports, show)

    def update(viewports: List[ViewportPanel], show: Boolean): Unit = {
      map.setVisible(node, viewports, show)
    }

    def apply(viewports: List[ViewportPanel]): Boolean = {
      map.isVisible(node, viewports)
    }

    def update(viewport: ViewportPanel, show: Boolean): Unit = {
      map.setVisible(node, viewport, show)
    }

    def apply(viewportPanel: ViewportPanel): Boolean = {
      map.isVisible(node, viewportPanel)
    }

    override def toString: String = {
      s"Visible [node=$node, hidden in ${map.toString(node)}]"
    }
  }

  class SceneNodeWithVisibility(node: SceneNode)(implicit frame: ScalismoFrame) {
    private val map: NodeVisibility = frame.sceneControl.nodeVisibility

    private val _visibility: Visible = new Visible(map, node)

    def visible: Visible = _visibility

    def visible_=(nv: Boolean): Unit = _visibility.visible = nv

  }

  object event {

    case class NodeVisibilityChanged(node: SceneNode, viewport: ViewportPanel) extends Event

  }

}

class NodeVisibility(frame: ScalismoFrame) extends ScalismoPublisher {
  private val hidden = new mutable.WeakHashMap[SceneNode, Set[ViewportPanel]]

  def isVisible(node: SceneNode, viewports: List[ViewportPanel] = allViewports): Boolean = {
    viewports.forall(v => isVisible(node, v))
  }

  def toString(node: SceneNode) = {
    hidden.get(node).toString
  }

  def isVisible(node: SceneNode, viewport: ViewportPanel): Boolean = {
    !hidden.get(node).exists(_.contains(viewport))
  }

  def setNodeVisibility(node: SceneNode, viewports: List[ViewportPanel], show: Boolean): Unit = {
    def nodeAndChildren(node: SceneNode): List[SceneNode] = {
      node :: node.children.flatMap(child => nodeAndChildren(child))
    }

    nodeAndChildren(node).foreach { node =>
      setVisible(node, viewports, show)
    }
  }

  private[NodeVisibility] def setVisible(node: SceneNode, viewport: ViewportPanel, show: Boolean): Unit = {
    setVisible(node, List(viewport), show)
  }

  private[NodeVisibility] def setVisible(node: SceneNode, viewports: List[ViewportPanel], show: Boolean): Unit = {
    val previous = hidden.getOrElse(node, Set.empty)
    val (added, removed) = if (show) (Set.empty, viewports.distinct) else (viewports.distinct, Set.empty)
    val current = (previous -- removed) ++ added
    if (current != previous) {
      if (current.isEmpty) {
        hidden.remove(node)
      } else {
        hidden(node) = current
      }
      (removed ++ added).foreach { viewport =>
        publishEvent(NodeVisibility.event.NodeVisibilityChanged(node, viewport))
      }
    }
  }

  private[NodeVisibility] def allViewports: List[ViewportPanel] = frame.perspectivesPanel.viewports

  private def handlePerspectiveChange(current: Perspective, previous: Perspective) = {
    val oldViewports = previous.viewports
    val newViewports = current.viewports

    val newHidden: List[(SceneNode, List[ViewportPanel])] = {
      val old3DCount = oldViewports.collect { case _3d: ViewportPanel3D => _3d }.length
      val new3DViews = newViewports.collect { case _3d: ViewportPanel3D => _3d }

      hidden.keys.toList.map { node =>
        if (!isVisible(node, oldViewports)) {
          // easy case: node was hidden in all viewports, so it just remains hidden
          (node, newViewports)
        } else {
          // we have to do some guesswork now.

          // We'll assume that if a node was hidden in a particular 2D axis view, it should remain hidden for that axis
          val axesToHide = oldViewports.collect { case _2d: ViewportPanel2D if !isVisible(node, _2d) => _2d.axis }.distinct
          val hide2D = newViewports.collect { case _2d: ViewportPanel2D if axesToHide.contains(_2d.axis) => _2d }

          // For 3D views, we'll assume that if a node was hidden in *strictly more* than half of the views, it should
          // remain hidden in *all* of the new 3D views. Otherwise, it gets shown.
          val hidden3D = oldViewports.collect { case _3d: ViewportPanel3D if !isVisible(node, _3d) => _3d }.length
          val hide3D = if (hidden3D > old3DCount / 2) new3DViews else Nil

          // return the viewports to hide the object in
          (node, hide2D ++ hide3D)
        }
      }
    }

    hidden.clear()
    newHidden.foreach {
      case (node, viewports) =>
        if (viewports.nonEmpty) {
          hidden(node) = viewports.toSet
          viewports.foreach { vp =>
            publishEvent(NodeVisibility.event.NodeVisibilityChanged(node, vp))
          }
        }
    }
  }

  def initialize(): Unit = {
    listenTo(frame.perspectivesPanel)
  }

  reactions += {
    case PerspectivesPanel.event.PerspectiveChanged(_, current, previous) if previous.isDefined => handlePerspectiveChange(current, previous.get)
  }
}
