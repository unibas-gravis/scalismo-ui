package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import org.statismo.stk.ui.Workspace
import javax.swing.tree.DefaultMutableTreeNode
import scala.swing.Component
import javax.swing.JTree
import scala.swing.ScrollPane
import scala.swing.Reactor
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeSelectionModel
import javax.swing.event.TreeSelectionListener
import javax.swing.event.TreeSelectionEvent
import org.statismo.stk.ui.SceneTreeObject
import collection.JavaConversions.enumerationAsScalaIterator
import org.statismo.stk.ui.Scene
import org.statismo.stk.ui.Nameable
import javax.swing.tree.TreePath

class ScenePropertiesPanel(val workspace: Workspace) extends BorderPanel with Reactor {

  private [ScenePropertiesPanel] class TreeNode(backend: SceneTreeObject) extends DefaultMutableTreeNode(backend) {
    override def getUserObject: SceneTreeObject = {
      super.getUserObject.asInstanceOf[SceneTreeObject]
    }
  }

  val scene = workspace.scene
  listenTo(scene)
  val root = new TreeNode(scene)
  val tree = new DefaultTreeModel(root)
  val listener = new TreeSelectionListener {
    def valueChanged(event: TreeSelectionEvent): Unit = {
      val jtree = event.getSource().asInstanceOf[JTree]
      val node = jtree.getLastSelectedPathComponent().asInstanceOf[TreeNode]
      val maybeSceneObject: Option[SceneTreeObject] = {
        if (node == null) None else {
          val obj = node.getUserObject()
          if (obj.isInstanceOf[SceneTreeObject]) Some(obj.asInstanceOf[SceneTreeObject])
          else None
        }
      }
      workspace.selectedObject = maybeSceneObject
    }
  }

  val jtree = new JTree(tree) {
      getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
      addTreeSelectionListener(listener)
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
    synchronizeTreeNode(scene, root)
  }

  def synchronizeTreeNode(backend: SceneTreeObject, frontend: TreeNode) {
    
    def frontendChildren = {
      List.fromIterator(frontend.children().map(_.asInstanceOf[TreeNode]))
    }
    
    val backendChildren = backend.children
    val obsoleteNodes = {
      frontendChildren.zipWithIndex.filterNot({ case (n, i) => backendChildren.exists({_ eq n.getUserObject}) }).map(_._1)
    }
    obsoleteNodes.foreach({ n =>
      tree.removeNodeFromParent(n)
    });
    val existingObjects = frontendChildren.map(_.getUserObject)
    val newObjectsWithIndex = backendChildren.zipWithIndex.filterNot({ case (o, i) => {
      val x = existingObjects.exists({_ eq o})
      x
    }})
    newObjectsWithIndex.foreach({
      case (obj, idx) => {
        println("creating node for " + obj)
        val node = new TreeNode(obj)
        tree.insertNodeInto(node, frontend, idx)
        // FIXME
        val p = node.getPath().map(_.asInstanceOf[Object])
        jtree.expandPath(new TreePath(p))
      }
    })
    
    backendChildren.zip(frontendChildren).foreach({
      case (back, front) => synchronizeTreeNode(back, front)
    })
  }
}