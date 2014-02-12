package org.statismo.stk.ui.swing

import java.awt.{Component => AComponent}
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.EventObject
import scala.Array.canBuildFrom
import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.collection.mutable.ArrayBuffer
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.Component
import scala.swing.Reactor
import scala.swing.ScrollPane
import org.statismo.stk.ui.Nameable
import org.statismo.stk.ui.Removeable
import org.statismo.stk.ui.Scene
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.Workspace
import org.statismo.stk.ui.swing.actions.AddImageRepresentationToStaticThreeDObjectAction
import org.statismo.stk.ui.swing.actions.AddMeshRepresentationToStaticThreeDObjectAction
import org.statismo.stk.ui.swing.actions.CreateShapeModelInstanceAction
import org.statismo.stk.ui.swing.actions.CreateStaticThreeDObjectFromImageAction
import org.statismo.stk.ui.swing.actions.CreateStaticThreeDObjectFromMeshAction
import org.statismo.stk.ui.swing.actions.LoadLoadableAction
import org.statismo.stk.ui.swing.actions.LoadShapeModelAction
import org.statismo.stk.ui.swing.actions.LoadShapeModelLandmarksAction
import org.statismo.stk.ui.swing.actions.RemoveRemoveableAction
import org.statismo.stk.ui.swing.actions.SaveSaveableAction
import org.statismo.stk.ui.swing.actions.SaveShapeModelLandmarksAction
import org.statismo.stk.ui.swing.actions.SceneTreePopupAction
import org.statismo.stk.ui.swing.actions.VisibilityAction
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import javax.swing.JRootPane
import java.awt.Frame

object SceneTreePanel {
  lazy val popupActions: ArrayBuffer[SceneTreePopupAction] = new ArrayBuffer[SceneTreePopupAction]() {
    this += new LoadShapeModelAction
    this += new LoadShapeModelLandmarksAction
    this += new SaveShapeModelLandmarksAction
    this += new CreateShapeModelInstanceAction
    this += new CreateStaticThreeDObjectFromMeshAction
    this += new CreateStaticThreeDObjectFromImageAction
    this += new AddMeshRepresentationToStaticThreeDObjectAction
    this += new AddImageRepresentationToStaticThreeDObjectAction
    this += new LoadLoadableAction
    this += new SaveSaveableAction
    this += new RemoveRemoveableAction
    this += new VisibilityAction
  }
}

class SceneTreePanel(val workspace: Workspace) extends BorderPanel with Reactor {

  private[SceneTreePanel] class TreeNode(backend: SceneTreeObject) extends DefaultMutableTreeNode(backend) {
    override def getUserObject: SceneTreeObject = {
      super.getUserObject.asInstanceOf[SceneTreeObject]
    }
  }

  val scene = workspace.scene
  listenTo(scene)

  lazy val popupActions: ArrayBuffer[SceneTreePopupAction] = SceneTreePanel.popupActions.clone

  val root = new TreeNode(scene)
  val tree = new DefaultTreeModel(root)

  def getTreeObjectForEvent(event: EventObject): Option[SceneTreeObject] = {
    val jtree = event.getSource().asInstanceOf[JTree]
    val node = jtree.getLastSelectedPathComponent().asInstanceOf[TreeNode]
    if (node == null) None else {
      val obj = node.getUserObject()
      if (obj.isInstanceOf[SceneTreeObject]) Some(obj.asInstanceOf[SceneTreeObject])
      else None
    }
  }
  val listener = new KeyAdapter with TreeSelectionListener {
    def valueChanged(event: TreeSelectionEvent): Unit = {
      workspace.selectedObject = getTreeObjectForEvent(event)
    }
    override def keyTyped(event: KeyEvent) {
      if (event.getKeyChar == '\u007f') { // delete
        val maybeRemoveable = getTreeObjectForEvent(event)
        if (maybeRemoveable.isDefined && maybeRemoveable.get.isInstanceOf[Removeable]) {
          val r = maybeRemoveable.get.asInstanceOf[Removeable]
          if (r.isCurrentlyRemoveable) {
            r.remove()
          }
        }
      }
    }
  }

  val mouseListener = new MouseAdapter() {
    override def mousePressed(event: MouseEvent) = handle(event)
    override def mouseReleased(event: MouseEvent) = handle(event)
    
    def handle(event: MouseEvent) = {
      if (event.isPopupTrigger) {
        val jtree = event.getSource().asInstanceOf[JTree]
        val x = event.getX()
        val y = event.getY()
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
      applicable.foreach { a =>
        val menu = a.createMenuItem(Some(target))
        if (menu.isDefined) {
          pop.add(menu.get.peer)
        } else {
        	pop.add(a.peer)
        }
      }
      pop.show(jtree, x, y)
      // needed because otherwise the popup may be hidden by the renderer window
      topmost.revalidate()
    }
  }
  
  val jtree = new JTree(tree) {
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
    addTreeSelectionListener(listener)
    addKeyListener(listener)
    addMouseListener(mouseListener)
    setExpandsSelectedPaths(true)
  }
  
  lazy val topmost = {
    import java.awt.{Component => AComponent}
    def top(c: AComponent): AComponent = {
      val p = c.getParent()
      if (p == null || c.isInstanceOf[Frame]) c else top(p)
    }
    top(jtree)
  }

  synchronizeTreeWithScene()

  val view: Component = {
    val ctree = new Component { override lazy val peer = jtree }
    val scroll = new ScrollPane(ctree)
    scroll
  }
  layout(view) = Center

  reactions += {
    case Scene.TreeTopologyChanged(s) => synchronizeTreeWithScene
    case Nameable.NameChanged(s) => { jtree.treeDidChange(); }
  }

  def synchronizeTreeWithScene() {
    val path = jtree.getSelectionPath()
    synchronizeTreeNode(scene, root)
    if (path != null) {
      jtree.setSelectionPath(path)
    } else {
      jtree.setSelectionRow(0)
    }
  }

  def synchronizeTreeNode(backend: SceneTreeObject, frontend: TreeNode) {

    def frontendChildren = List.fromIterator(frontend.children.map(_.asInstanceOf[TreeNode]))

    val backendChildren = backend.children
    val obsoleteIndexes = frontendChildren.zipWithIndex.filterNot({ case (n, i) => backendChildren.exists ( _ eq n.getUserObject )}).map(_._1)
    obsoleteIndexes.foreach(tree.removeNodeFromParent(_));
    
    val existingObjects = frontendChildren.map(_.getUserObject)
    val newObjectsWithIndex = backendChildren.zipWithIndex.filterNot { case (o, i) => existingObjects.exists(_ eq o )}
    
    newObjectsWithIndex.foreach({
      case (obj, idx) => {
        val node = new TreeNode(obj)
        tree.insertNodeInto(node, frontend, idx)
        val p = node.getPath().map(_.asInstanceOf[Object])
        jtree.setSelectionPath(new TreePath(p))
      }
    })

    backendChildren.zip(frontendChildren).foreach { case (back, front) => synchronizeTreeNode(back, front) }
  }
}