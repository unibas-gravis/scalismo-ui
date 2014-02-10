package org.statismo.stk.ui.swing.actions

import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.StaticThreeDObject
import org.statismo.stk.ui.StaticThreeDObject
import org.statismo.stk.ui.StaticThreeDObjects
import org.statismo.stk.ui.StaticMesh
import java.io.File
import scala.util.Try
import org.statismo.stk.ui.FileIoMetadata
import org.statismo.stk.ui.ThreeDRepresentations
import org.statismo.stk.ui.StaticThreeDObject

private [actions] abstract class CreateStaticThreeDObjectFromSomethingAction(name: String) extends SceneTreePopupAction(name) {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[StaticThreeDObjects]
  }

  def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val parent = context.get.asInstanceOf[StaticThreeDObjects]
      def doLoad(file: File): Try[Unit] = load(file, parent)
      new LoadSceneTreeObjectAction(doLoad, StaticMesh).apply
    }
  }
  
  def load(file: File, parent: StaticThreeDObjects): Try[Unit]
  def metadata: FileIoMetadata
}

class CreateStaticThreeDObjectFromMeshAction extends CreateStaticThreeDObjectFromSomethingAction("Create from Mesh file...") {
  override val metadata = StaticMesh
  def load(file: File, parent: StaticThreeDObjects): Try[Unit] = {
        for (m <- StaticMesh(file)(parent.scene)) yield {}
  }
}

private [actions] abstract class AddRepresentationToStaticThreeDObjectAction(name: String) extends SceneTreePopupAction(name) {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ThreeDRepresentations] && context.get.asInstanceOf[ThreeDRepresentations].parent.isInstanceOf[StaticThreeDObject]
  }

  def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val parent = context.get.asInstanceOf[ThreeDRepresentations]
      def doLoad(file: File): Try[Unit] = load(file, parent)
      new LoadSceneTreeObjectAction(doLoad, StaticMesh).apply
    }
  }
  
  def load(file: File, parent: ThreeDRepresentations): Try[Unit]
  def metadata: FileIoMetadata
}

class AddMeshRepresentationToStaticThreeDObjectAction extends AddRepresentationToStaticThreeDObjectAction("Add from Mesh file...") {
  override val metadata = StaticMesh
  def load(file: File, parent: ThreeDRepresentations): Try[Unit] = {
        for (m <- StaticMesh(file, Some(parent), file.getName())(parent.scene)) yield {}
  }
}

