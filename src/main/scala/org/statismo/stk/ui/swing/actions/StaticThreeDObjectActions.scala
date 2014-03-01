package org.statismo.stk.ui.swing.actions

import java.io.File
import scala.util.Success
import scala.util.Try
import org.statismo.stk.ui.FileIoMetadata
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.StaticImage
import org.statismo.stk.ui.StaticMesh
import org.statismo.stk.ui.StaticThreeDObject
import org.statismo.stk.ui.StaticThreeDObjects
import org.statismo.stk.ui.ThreeDRepresentations
import org.statismo.stk.ui.ThreeDObject
import org.statismo.stk.ui.StaticThreeDObject

private[actions] abstract class CreateStaticThreeDObjectFromSomethingAction(name: String) extends SceneTreePopupAction(name) {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[StaticThreeDObjects]
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val parent = context.get.asInstanceOf[StaticThreeDObjects]
      def doLoad(file: File): Try[Unit] = load(file, parent)
      new LoadAction(doLoad, metadata).apply()
    }
  }

  def load(file: File, parent: StaticThreeDObjects): Try[Unit]
  def metadata: FileIoMetadata
}

class CreateStaticThreeDObjectFromMeshAction extends CreateStaticThreeDObjectFromSomethingAction("Create from Mesh file...") {
  override val metadata = StaticMesh
  def load(file: File, parent: StaticThreeDObjects): Try[Unit] = {
    StaticMesh(file)(parent.scene).map(ok => Success(()))
  }
}

class CreateStaticThreeDObjectFromImageAction extends CreateStaticThreeDObjectFromSomethingAction("Create from Image file...") {
  override val metadata = StaticImage
  def load(file: File, parent: StaticThreeDObjects): Try[Unit] = {
    StaticImage(file)(parent.scene).map(ok => Success(()))
  }
}

private[actions] abstract class AddRepresentationToStaticThreeDObjectAction(name: String) extends SceneTreePopupAction(name) {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[StaticThreeDObject]
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val parent = context.get.asInstanceOf[StaticThreeDObject]
      def doLoad(file: File): Try[Unit] = load(file, parent)
      new LoadAction(doLoad, metadata).apply()
    }
  }

  def load(file: File, parent: StaticThreeDObject): Try[Unit]
  def metadata: FileIoMetadata
}

class AddMeshRepresentationToStaticThreeDObjectAction extends AddRepresentationToStaticThreeDObjectAction("Add Representation from Mesh file...") {
  override val metadata = StaticMesh
  def load(file: File, parent: StaticThreeDObject): Try[Unit] = {
    StaticMesh(file, Some(parent), file.getName)(parent.scene).map(ok => Success(()))
  }
}

class AddImageRepresentationToStaticThreeDObjectAction extends AddRepresentationToStaticThreeDObjectAction("Add Representation from Image file...") {
  override val metadata = StaticImage
  def load(file: File, parent: StaticThreeDObject): Try[Unit] = {
    StaticImage(file, Some(parent), file.getName)(parent.scene).map(ok => Success(()))
  }
}

