package org.statismo.stk.ui.view.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import scala.swing.Button
import org.statismo.stk.ui.Workspace
import javax.swing.tree.DefaultMutableTreeNode
import scala.swing.Component
import javax.swing.JTree
import scala.swing.ScrollPane
import scala.swing.Reactor
import org.statismo.stk.ui.SceneChanged
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeSelectionModel
import javax.swing.event.TreeSelectionListener
import javax.swing.event.TreeSelectionEvent
import org.statismo.stk.ui.SceneObject

class ScenePropertiesPanel(val workspace: Workspace) extends BorderPanel with Reactor {
  //layout(new Button("scene objects")) = Center
  val root = new DefaultMutableTreeNode("Scene")
  val tree = new DefaultTreeModel(root)
  val listener = new TreeSelectionListener {
    def valueChanged(event: TreeSelectionEvent): Unit = {
      val jtree = event.getSource().asInstanceOf[JTree]
      val node = jtree.getLastSelectedPathComponent().asInstanceOf[DefaultMutableTreeNode]
      val maybeSceneObject: Option[SceneObject] = {
        if (node == null) None else {
          val obj = node.getUserObject()
          if (obj.isInstanceOf[SceneObject]) Some(obj.asInstanceOf[SceneObject])
          else None
        }
      }
      workspace.selectedObject = maybeSceneObject
    }
   }
  populateTree()
  val view: Component = {
    val jtree = new JTree(tree) {
      getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
      addTreeSelectionListener(listener)
    }
    val ctree = new Component { override lazy val peer = jtree }
    val scroll = new ScrollPane(ctree)
    scroll
  }
  layout(view) = Center
  listenTo(workspace.scene)

  reactions += {
    case SceneChanged() => populateTree
  }

  def populateTree() {
    {0 until root.getChildCount()}.reverse.foreach ({ i =>
      tree.removeNodeFromParent(root.getChildAt(i).asInstanceOf[MutableTreeNode])
    })
    workspace.scene.objects.foreach({ o =>
      val node = new DefaultMutableTreeNode(o)
      tree.insertNodeInto(node, root, root.getChildCount())
    })
  }
}