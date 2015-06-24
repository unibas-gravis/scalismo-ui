package scalismo.ui.swing.actions.scenetree

import java.io.File

import scalismo.ui.swing.actions.LoadAction
import scalismo.ui.{ FileIoMetadata, SceneTreeObject, StaticImage3D, StaticMesh, StaticThreeDObject, StaticThreeDObjects }

import scala.util.{ Success, Try }

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
    StaticMesh.tryCreate(file)(parent.scene).map(ok => Success(()))
  }
}

class CreateStaticThreeDObjectFromImageAction extends CreateStaticThreeDObjectFromSomethingAction("Create from Image file...") {
  override val metadata = StaticImage3D

  def load(file: File, parent: StaticThreeDObjects): Try[Unit] = {
    StaticImage3D.tryCreate(file)(parent.scene).map(ok => Success(()))
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
    StaticMesh.createFromFile(file, Some(parent), file.getName)(parent.scene).map(ok => Success(()))
  }
}

class AddImageRepresentationToStaticThreeDObjectAction extends AddRepresentationToStaticThreeDObjectAction("Add Representation from Image file...") {
  override val metadata = StaticImage3D

  def load(file: File, parent: StaticThreeDObject): Try[Unit] = {
    StaticImage3D.createFromFile(file, Some(parent), file.getName)(parent.scene).map(ok => Success(()))
  }
}

