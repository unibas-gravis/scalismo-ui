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
import org.statismo.stk.ui.StaticImage
import scala.util.Success

private[actions] abstract class CreateStaticThreeDObjectFromSomethingAction(name: String) extends SceneTreePopupAction(name) {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[StaticThreeDObjects]
  }

  def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val parent = context.get.asInstanceOf[StaticThreeDObjects]
      def doLoad(file: File): Try[Unit] = load(file, parent)
      new LoadSceneTreeObjectAction(doLoad, metadata).apply
    }
  }

  def load(file: File, parent: StaticThreeDObjects): Try[Unit]
  def metadata: FileIoMetadata
}

class CreateStaticThreeDObjectFromMeshAction extends CreateStaticThreeDObjectFromSomethingAction("Create from Mesh file...") {
  override val metadata = StaticMesh
  def load(file: File, parent: StaticThreeDObjects): Try[Unit] = {
    StaticMesh(file)(parent.scene).map(ok => Success())
  }
}

class CreateStaticThreeDObjectFromImageAction extends CreateStaticThreeDObjectFromSomethingAction("Create from Image file...") {
  override val metadata = StaticImage
  def load(file: File, parent: StaticThreeDObjects): Try[Unit] = {
    StaticImage(file)(parent.scene).map(ok => Success())
  }
}

private[actions] abstract class AddRepresentationToStaticThreeDObjectAction(name: String) extends SceneTreePopupAction(name) {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ThreeDRepresentations] && context.get.asInstanceOf[ThreeDRepresentations].parent.isInstanceOf[StaticThreeDObject]
  }

  def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val parent = context.get.asInstanceOf[ThreeDRepresentations]
      def doLoad(file: File): Try[Unit] = load(file, parent)
      new LoadSceneTreeObjectAction(doLoad, metadata).apply
    }
  }

  def load(file: File, parent: ThreeDRepresentations): Try[Unit]
  def metadata: FileIoMetadata
}

class AddMeshRepresentationToStaticThreeDObjectAction extends AddRepresentationToStaticThreeDObjectAction("Add from Mesh file...") {
  override val metadata = StaticMesh
  def load(file: File, parent: ThreeDRepresentations): Try[Unit] = {
    StaticMesh(file, Some(parent), file.getName())(parent.scene).map(ok => Success())
  }
}

class AddImageRepresentationToStaticThreeDObjectAction extends AddRepresentationToStaticThreeDObjectAction("Add from Image file...") {
  override val metadata = StaticImage
  def load(file: File, parent: ThreeDRepresentations): Try[Unit] = {
    StaticImage(file, Some(parent), file.getName())(parent.scene).map(ok => Success())
  }
}

