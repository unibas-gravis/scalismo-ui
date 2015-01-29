package org.statismo.stk.ui.swing.actions.scenetree

import java.io.File

import scala.util.Success
import scala.util.Try

import org.statismo.stk.ui._
import org.statismo.stk.ui.swing.actions.{SaveAction, LoadAction}

class LoadShapeModelLandmarksAction extends SceneTreePopupAction("Load landmarks from file...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModel] && context.get.asInstanceOf[ShapeModel].landmarks.isCurrentlyLoadable
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val load = context.get.asInstanceOf[ShapeModel].landmarks
      def doLoad(file: File): Try[Unit] = {
        load.loadFromFile(file)
      }
      new LoadAction(doLoad, load.loadableMetadata).apply()
    }
  }
}

class SaveShapeModelLandmarksAction extends SceneTreePopupAction("Save landmarks to file...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModel] && context.get.asInstanceOf[ShapeModel].landmarks.isCurrentlySaveable
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val save = context.get.asInstanceOf[ShapeModel].landmarks
      def doSave(file: File): Try[Unit] = {
        save.saveToFile(file)
      }
      new SaveAction(doSave, save.saveableMetadata).apply()
    }
  }
}

class LoadShapeModelAction extends SceneTreePopupAction("Load Shape Model from file...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModels]
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val shapes = context.get.asInstanceOf[ShapeModels]
      def doLoad(file: File): Try[Unit] = {
        ShapeModel.tryCreate(file)(shapes.scene).map(ok => Success(()))
      }
      new LoadAction(doLoad, ShapeModel).apply()
    }
  }
}

class CreateShapeModelInstanceAction extends SceneTreePopupAction("Create new Instance") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModel]
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val model = context.get.asInstanceOf[ShapeModel]
      model.instances.create()
    }
  }
}

class RemoveAllShapeModelInstancesAction extends SceneTreePopupAction("Remove all Instances") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModel] && context.get.asInstanceOf[ShapeModel].instances.children.nonEmpty
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val model = context.get.asInstanceOf[ShapeModel]
      model.instances.removeAll()
    }
  }
}

class AddReferenceAsStaticObjectAction extends SceneTreePopupAction("Add Reference Mesh as new Static Object") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModel]
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val model = context.get.asInstanceOf[ShapeModel]
      StaticMesh.createFromPeer(model.peer.referenceMesh, None, Some(s"${model.name}-reference"))(model.parent.scene)
    }
  }
}

class CloneInstanceAsStaticObjectAction extends SceneTreePopupAction("Clone as new Static Object") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context match {
      case Some(r: ShapeModelInstance.MeshRepresentation) => true
      case _ => false
    }
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val mesh = context.get.asInstanceOf[ShapeModelInstance.MeshRepresentation]
      StaticMesh.createFromPeer(mesh.peer, None, Some(s"${mesh.parent.name}-copy"))(mesh.parent.scene)
    }
  }
}

