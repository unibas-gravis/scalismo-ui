package scalismo.ui.view

import java.awt
import java.awt.event.{ MouseEvent, MouseAdapter }
import java.util.EventObject
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree._
import javax.swing.{ Icon, JTree }

import scalismo.ui.model.SceneNode
import scalismo.ui.model.capabilities.CollapsableView
import scalismo.ui.view.NodesPanel.{ SceneNodeCellRenderer, ViewNode }

import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.collection.immutable
import scala.swing.{ BorderPanel, Component, ScrollPane }

object NodesPanel {

  class ViewNode(backend: SceneNode) extends DefaultMutableTreeNode(backend) {
    override def getUserObject: SceneNode = {
      super.getUserObject.asInstanceOf[SceneNode]
    }
  }

  class SceneNodeCellRenderer extends DefaultTreeCellRenderer {

    class Icons(open: Icon, closed: Icon, leaf: Icon) {
      // the invocation context is a call to getTreeCellRendererComponent().
      def apply() = {
        setOpenIcon(open)
        setClosedIcon(closed)
        setLeafIcon(leaf)
      }
    }

    object Icons {
      /* note: this uses the "closed" icon for leaves. */
      final val DefaultIcons = new Icons(HighDpi.scaleIcon(getDefaultOpenIcon), HighDpi.scaleIcon(getDefaultClosedIcon), HighDpi.scaleIcon(getDefaultClosedIcon))

      def getForNode(node: SceneNode): Icons = {
        node match {
          //          case vis: VisualizableSceneTreeObject[_] =>
          //            IconFactory.iconFor(vis) match {
          //              case None => DefaultIcons
          //              case Some(icon) =>
          //                new Icons(icon, icon, icon)
          //            }
          case _ => DefaultIcons
        }
      }
    }

    private var recursingInGetRendererComponent = false

    override def getTreeCellRendererComponent(tree: JTree, value: scala.Any, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): awt.Component = {
      val sceneNode = value.asInstanceOf[ViewNode].getUserObject

      Icons.getForNode(sceneNode).apply()

      val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

      /* Next,we try to set the width of the component to extend (almost) to the right edge of the containing tree.
       * This has two advantages: first, it should get rid of the annoying ellipses when renaming a node (e.g. "A" -> "ABC"
       * would result in the tree showing "..." instead). Second, it allows to right-click anywhere in the row to get the popup menu.
       */

      if (component == this) {
        // the tree.getPathBounds() method used below results in another call to this method, which will create a stack overflow if we don't handle it.
        if (!recursingInGetRendererComponent) {
          recursingInGetRendererComponent = true
          val Margin = 3
          val bounds = tree.getPathBounds(tree.getPathForRow(row))
          val treeWidth = tree.getWidth
          if (bounds != null && treeWidth - Margin > bounds.x) {
            val pref = component.getPreferredSize
            pref.width = treeWidth - Margin - bounds.x
            setPreferredSize(pref)
          }
          recursingInGetRendererComponent = false
        }
      }
      component
    }

    // just for visual debugging, if needed
    //setBorder(BorderFactory.createLineBorder(Color.BLACK))
  }

}

class NodesPanel(val frame: ScalismoFrame) extends BorderPanel {
  val scene = frame.scene

  val rootNode = new ViewNode(scene)

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
          val node = getSceneNodeForEvent(event)
          if (node.isDefined) {
            //FIXME
            synchronizeWholeTree()
            //handlePopup(obj.get, x, y)
          }
        }
      }
    }
  }

  val treeModel = new DefaultTreeModel(rootNode)

  val tree = new JTree(treeModel) {
    setCellRenderer(new SceneNodeCellRenderer)
    getSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
    //    addTreeSelectionListener(listener)
    //    addKeyListener(listener)
    addMouseListener(mouseListener)
    setExpandsSelectedPaths(true)
    setLargeModel(true)
  }

  val scroll = new ScrollPane(Component.wrap(tree))

  layout(scroll) = BorderPanel.Position.Center

  synchronizeWholeTree()

  def getSceneNodeForEvent(event: EventObject): Option[SceneNode] = {
    val jtree = event.getSource.asInstanceOf[JTree]
    val node = jtree.getLastSelectedPathComponent.asInstanceOf[ViewNode]
    if (node == null) None
    else {
      val obj = node.getUserObject()
      obj match {
        case sNode: SceneNode => Some(sNode)
        case _ => None
      }
    }
  }

  def repaintTree(): Unit = {
    // try to force the tree to invalidate cached node sizes
    tree.getUI match {
      case ui: BasicTreeUI => ui.setLeftChildIndent(ui.getLeftChildIndent)
      case _ => //don't know how to handle
    }
    tree.treeDidChange()
  }

  def synchronizeWholeTree(): Unit = {
    println("synchronizing tree")
    // save user's selection for later
    val path = tree.getSelectionPath
    synchronizeSingleNode(scene, rootNode)
    if (path != null) {
      println(path.getLastPathComponent)
    } else {
      tree.setSelectionRow(0)
    }

    repaintTree()
  }

  def synchronizeSingleNode(backend: SceneNode, frontend: ViewNode): Unit = {
    def frontendChildren = frontend.children.map(_.asInstanceOf[ViewNode]).toList

    def nodeOrNodeChildren(node: SceneNode): Seq[SceneNode] = {
      node match {
        case coll: CollapsableView if coll.isViewCollapsed => node.children.flatMap(nodeOrNodeChildren)
        case _ => List(node)
      }
    }

    val backendChildren = backend.children.flatMap(nodeOrNodeChildren)

    val obsoleteIndexes = frontendChildren.zipWithIndex.filterNot({
      case (n, i) => backendChildren.exists(_ eq n.getUserObject)
    }).map(_._1)

    obsoleteIndexes.foreach(treeModel.removeNodeFromParent(_))

    val existingObjects = frontendChildren.map(_.getUserObject)
    val newObjectsWithIndex = backendChildren.zipWithIndex.filterNot {
      case (o, i) => existingObjects.exists(_ eq o)
    }

    newObjectsWithIndex.foreach({
      case (obj, idx) =>
        val node = new ViewNode(obj)
        treeModel.insertNodeInto(node, frontend, idx)
        val p = node.getPath.map(_.asInstanceOf[Object])
        tree.setSelectionPath(new TreePath(p))
    })

    backendChildren.zip(frontendChildren).foreach {
      case (back, front) => synchronizeSingleNode(back, front)
    }

  }

}
