package scalismo.ui.api

import scalismo.ui.model._

trait SimpleAPI {

  protected[api] def scene: Scene

  def createGroup(groupName: String): Group = Group(scene.groups.add(groupName))

  def show[A](a: A, name: String)(implicit showInScene: ShowInScene[A]): showInScene.View = showInScene.showInScene(a, name, defaultGroup)

  def show[A](group: Group, a: A, name: String)(implicit showInScene: ShowInScene[A]): showInScene.View = showInScene.showInScene(a, name, group)

  def addTransformation[T](g: Group, t: T, name: String)(implicit showInScene: ShowInScene[T]): showInScene.View = {
    showInScene.showInScene(t, name, g)
  }

  def filter[V <: ObjectView: FindInScene](pred: V => Boolean): Seq[V] = {
    filterSceneNodes[V](scene, pred)
  }

  def filter[V <: ObjectView: FindInScene](group: Group, pred: V => Boolean): Seq[V] = {
    filterSceneNodes[V](group.peer, pred)
  }

  def find[V <: ObjectView: FindInScene](pred: V => Boolean): Option[V] =
    filter[V](pred).headOption

  def find[V <: ObjectView: FindInScene](group: Group, pred: V => Boolean): Option[V] =
    filter[V](pred).headOption

  def onNodeAdded[A <: ObjectView: HandleCallback, R](g: Group, f: A => R): Unit = {
    HandleCallback[A].registerOnAdd(g, f)
  }

  def onNodeRemoved[A <: ObjectView: HandleCallback, R](g: Group, f: A => R): Unit = {
    HandleCallback[A].registerOnRemove(g, f)
  }

  private def defaultGroup: Group = {
    val groupNode = scene.groups.find(g => g.name == "group")
      .getOrElse(scene.groups.add("group"))
    Group(groupNode)
  }

  private def filterSceneNodes[V <: ObjectView: FindInScene](node: SceneNode, pred: V => Boolean): Seq[V] = {

    val resFromSubNodes = node.children.flatMap(child => filterSceneNodes(child, pred))

    val find = FindInScene[V]

    val head = find.createView(node) match {
      case Some(v) => if (pred(v)) Seq[V](v) else Seq[V]()
      case None => Seq[V]()
    }

    head ++ resFromSubNodes
  }

}

