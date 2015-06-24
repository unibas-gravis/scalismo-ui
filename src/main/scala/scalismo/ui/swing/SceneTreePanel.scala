package scalismo.ui.swing

import java.awt
import java.awt.Frame
import java.awt.event.{ KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }
import java.util.EventObject
import javax.swing.event.{ TreeSelectionEvent, TreeSelectionListener }
import javax.swing.tree._
import javax.swing.{ Icon, JPopupMenu, JTree }

import scalismo.ui._
import scalismo.ui.swing.SceneTreePanel.TreeNode
import scalismo.ui.swing.actions.scenetree._
import scalismo.ui.visualization.icons.IconFactory
import scalismo.ui.visualization.{ VisualizableSceneTreeObject, VisualizationProperty }

import scala.Array.canBuildFrom
import scala.annotation.tailrec
import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.swing.BorderPanel.Position.Center
import scala.swing.{ BorderPanel, Component, Reactor, ScrollPane }

object SceneTreePanel {
  lazy val popupActions: Seq[SceneTreePopupAction] = Seq(
    new ReloadReloadableAction,
    new LoadLoadableAction,
    new SaveSaveableAction,
    new RemoveRemoveableAction,
    new LoadShapeModelAction,
    new LoadShapeModelLandmarksAction,
    new SaveShapeModelLandmarksAction,
    new CreateShapeModelInstanceAction,
    new RemoveAllShapeModelInstancesAction,
    new AddReferenceAsStaticObjectAction,
    new CloneInstanceAsStaticObjectAction,
    new CreateStaticThreeDObjectFromMeshAction,
    new CreateStaticThreeDObjectFromImageAction,
    new AddMeshRepresentationToStaticThreeDObjectAction,
    new AddImageRepresentationToStaticThreeDObjectAction,
    new VisibilityAction,
    new RenameNameableAction)

  private[SceneTreePanel] class TreeNode(backend: SceneTreeObject) extends DefaultMutableTreeNode(backend) {
    override def getUserObject: SceneTreeObject = {
      super.getUserObject.asInstanceOf[SceneTreeObject]
    }
  }

  class CellRenderer(implicit val scene: Scene) extends DefaultTreeCellRenderer {

    object Icons {
      /* note: this uses the "closed" icon for leaves. */
      final val DefaultIcons = new Icons(getDefaultOpenIcon, getDefaultClosedIcon, getDefaultClosedIcon)

      def getForNode(node: SceneTreeObject): Icons = {
        node match {
          case vis: VisualizableSceneTreeObject[_] =>
            IconFactory.iconFor(vis) match {
              case None => DefaultIcons
              case Some(icon) =>
                new Icons(icon, icon, icon)
            }
          case _ => DefaultIcons
        }
      }
    }

    class Icons(open: Icon, closed: Icon, leaf: Icon) {
      // the invocation context is a call to getTreeCellRendererComponent().
      def apply() = {
        setOpenIcon(open)
        setClosedIcon(closed)
        setLeafIcon(leaf)
      }
    }

    override def getTreeCellRendererComponent(tree: JTree, value: scala.Any, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): awt.Component = {
      val sceneTreeObject = value.asInstanceOf[TreeNode].getUserObject

      Icons.getForNode(sceneTreeObject).apply()

      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
    }
  }

}

class SceneTreePanel(val workspace: Workspace) extends BorderPanel with Reactor {

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

    override def keyTyped(event: KeyEvent): Unit = {
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
    if (applicable.nonEmpty) {
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
    setCellRenderer(new SceneTreePanel.CellRenderer()(scene))
    getSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
    addTreeSelectionListener(listener)
    addKeyListener(listener)
    addMouseListener(mouseListener)
    setExpandsSelectedPaths(true)
  }

  lazy val topmost = {
    import java.awt.{ Component => AComponent }
    @tailrec
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

      // this is essentially a hack, to ensure that the tree gets repainted
      // whenever a visualization property changes
      listenTo(VisualizationProperty)
      reactions += {
        case VisualizationProperty.ValueChanged(_) => jtree.repaint()
      }
    }
    val scroll = new ScrollPane(ctree)
    scroll
  }
  layout(view) = Center

  reactions += {
    case Scene.TreeTopologyChanged(s) => synchronizeTreeWithScene()
    case Nameable.NameChanged(s) => jtree.treeDidChange()
  }

  def synchronizeTreeWithScene(): Unit = {
    val path = jtree.getSelectionPath
    synchronizeTreeNode(scene, root)
    if (path != null) {
      jtree.setSelectionPath(path)
    } else {
      jtree.setSelectionRow(0)
    }
  }

  protected def synchronizeTreeNode(backend: SceneTreeObject, frontend: TreeNode): Unit = {

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
