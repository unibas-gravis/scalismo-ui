package org.statismo.stk.ui.swing

import java.awt.Frame
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.EventObject

import scala.Array.canBuildFrom
import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.Component
import scala.swing.Reactor
import scala.swing.ScrollPane

import org.statismo.stk.ui._

import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import org.statismo.stk.ui.swing.actions.scenetree._
import scala.Some

object SceneTreePanel {
  lazy val popupActions: Seq[SceneTreePopupAction] = Seq(
    new LoadLoadableAction,
    new SaveSaveableAction,
    new RemoveRemoveableAction,
    new LoadShapeModelAction,
    new LoadShapeModelLandmarksAction,
    new SaveShapeModelLandmarksAction,
    new CreateShapeModelInstanceAction,
    new RemoveAllShapeModelInstancesAction,
    new AddReferenceMeshAction,
    new CreateStaticThreeDObjectFromMeshAction,
    new CreateStaticThreeDObjectFromImageAction,
    new AddMeshRepresentationToStaticThreeDObjectAction,
    new AddImageRepresentationToStaticThreeDObjectAction,
    new VisibilityAction,
    new RenameNameableAction)
}

class SceneTreePanel(val workspace: Workspace) extends BorderPanel with Reactor {

  private[SceneTreePanel] class TreeNode(backend: SceneTreeObject) extends DefaultMutableTreeNode(backend) {
    override def getUserObject: SceneTreeObject = {
      super.getUserObject.asInstanceOf[SceneTreeObject]
    }
  }

  val scene = workspace.scene
  listenTo(scene)

  lazy val popupActions: Seq[SceneTreePopupAction] = SceneTreePanel.popupActions

  val root = new TreeNode(scene)
  val tree = new DefaultTreeModel(root)

  def getTreeObjectForEvent(event: EventObject): Option[SceneTreeObject] = {
    val jtree = event.getSource.asInstanceOf[JTree]
    val node = jtree.getLastSelectedPathComponent.asInstanceOf[TreeNode]
    if (node == null) None
    else {
      val obj = node.getUserObject()
      obj match {
        case sceneTreeObject: SceneTreeObject => Some(sceneTreeObject)
        case _ => None
      }
    }
  }

  val listener = new KeyAdapter with TreeSelectionListener {
    def valueChanged(event: TreeSelectionEvent): Unit = {
      workspace.selectedObject = getTreeObjectForEvent(event)
    }

    override def keyTyped(event: KeyEvent) {
      if (event.getKeyChar == '\u007f') {
        // delete
        val maybeRemoveable = getTreeObjectForEvent(event)
        maybeRemoveable match {
          case Some(r: Removeable) =>
            if (r.isCurrentlyRemoveable) {
              r.remove()
            }
          case Some(r: RemoveableChildren) => r.removeAll()
          case _ =>
        }
      }
    }
  }

  val mouseListener = new MouseAdapter() {
    override def mousePressed(event: MouseEvent) = handle(event)

    override def mouseReleased(event: MouseEvent) = handle(event)

    def handle(event: MouseEvent) = {
      if (event.isPopupTrigger) {
        val jtree = event.getSource.asInstanceOf[JTree]
        val x = event.getX
        val y = event.getY
        val path = jtree.getPathForLocation(x, y)
        if (path != null) {
          jtree.setSelectionPath(path)
          val obj = getTreeObjectForEvent(event)
          if (obj.isDefined) {
            handlePopup(obj.get, x, y)
          }
        }
      }
    }
  }

  def handlePopup(target: SceneTreeObject, x: Int, y: Int): Unit = {
    val applicable = popupActions.filter(a => a.setContext(Some(target)))
    if (!applicable.isEmpty) {
      val pop = new JPopupMenu()
      applicable.foreach {
        a =>
          val menu = a.createMenuItem(Some(target))
          if (menu.isDefined) {
            pop.add(menu.get.peer)
          } else {
            pop.add(a.peer)
          }
      }
      pop.show(jtree, x, y)
      // needed because otherwise the popup may be hidden by the renderer window
      topmost.invalidate()
      topmost.validate()
    }
  }

  val jtree = new JTree(tree) {
    getSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
    addTreeSelectionListener(listener)
    addKeyListener(listener)
    addMouseListener(mouseListener)
    setExpandsSelectedPaths(true)
  }

  lazy val topmost = {
    import java.awt.{Component => AComponent}
    def top(c: AComponent): AComponent = {
      val p = c.getParent
      if (p == null || c.isInstanceOf[Frame]) c else top(p)
    }
    top(jtree)
  }

  synchronizeTreeWithScene()

  val view: Component = {
    val ctree = new Component {
      override lazy val peer = jtree
    }
    val scroll = new ScrollPane(ctree)
    scroll
  }
  layout(view) = Center

  reactions += {
    case Scene.TreeTopologyChanged(s) => synchronizeTreeWithScene()
    case Nameable.NameChanged(s) => jtree.treeDidChange()
  }

  def synchronizeTreeWithScene() {
    val path = jtree.getSelectionPath
    synchronizeTreeNode(scene, root)
    if (path != null) {
      jtree.setSelectionPath(path)
    } else {
      jtree.setSelectionRow(0)
    }
  }

  protected def synchronizeTreeNode(backend: SceneTreeObject, frontend: TreeNode) {

    def frontendChildren = frontend.children.map(_.asInstanceOf[TreeNode]).toList

    val backendChildren = backend.children
    val obsoleteIndexes = frontendChildren.zipWithIndex.filterNot({
      case (n, i) => backendChildren.exists(_ eq n.getUserObject)
    }).map(_._1)
    obsoleteIndexes.foreach(tree.removeNodeFromParent(_))

    val existingObjects = frontendChildren.map(_.getUserObject)
    val newObjectsWithIndex = backendChildren.zipWithIndex.filterNot {
      case (o, i) => existingObjects.exists(_ eq o)
    }

    newObjectsWithIndex.foreach({
      case (obj, idx) =>
        val node = new TreeNode(obj)
        tree.insertNodeInto(node, frontend, idx)
        val p = node.getPath.map(_.asInstanceOf[Object])
        jtree.setSelectionPath(new TreePath(p))
    })

    backendChildren.zip(frontendChildren).foreach {
      case (back, front) => synchronizeTreeNode(back, front)
    }
  }
}