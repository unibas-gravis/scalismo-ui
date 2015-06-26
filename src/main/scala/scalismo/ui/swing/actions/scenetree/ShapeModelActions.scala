package scalismo.ui.swing.actions.scenetree

import java.io.File

import scalismo.ui._
import scalismo.ui.swing.actions.{ LoadAction, SaveAction }

import scala.util.{ Success, Try }

class LoadShapeModelLandmarksAction extends SceneTreePopupAction("Load landmarks from file...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModelView] && context.get.asInstanceOf[ShapeModelView].landmarks.isCurrentlyLoadable
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val load = context.get.asInstanceOf[ShapeModelView].landmarks
      def doLoad(file: File): Try[Unit] = {
        load.loadFromFile(file)
      }
      new LoadAction(doLoad, load.loadableMetadata).apply()
    }
  }
}

class SaveShapeModelLandmarksAction extends SceneTreePopupAction("Save landmarks to file...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModelView] && context.get.asInstanceOf[ShapeModelView].landmarks.isCurrentlySaveable
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val save = context.get.asInstanceOf[ShapeModelView].landmarks
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
        ShapeModelView.tryCreate(file)(shapes.scene).map(ok => Success(()))
      }
      new LoadAction(doLoad, ShapeModelView).apply()
    }
  }
}

class CreateShapeModelInstanceAction extends SceneTreePopupAction("Create new Instance") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModelView]
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val model = context.get.asInstanceOf[ShapeModelView]
      model.instances.create()
    }
  }
}

class RemoveAllShapeModelInstancesAction extends SceneTreePopupAction("Remove all Instances") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModelView] && context.get.asInstanceOf[ShapeModelView].instances.children.nonEmpty
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val model = context.get.asInstanceOf[ShapeModelView]
      model.instances.removeAll()
    }
  }
}

class AddReferenceAsStaticObjectAction extends SceneTreePopupAction("Add Reference Mesh as new Static Object") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModelView]
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val model = context.get.asInstanceOf[ShapeModelView]
      MeshView.createFromUnderlying(model.underlying.referenceMesh, None, Some(s"${model.name}-reference"))(model.parent.scene)
    }
  }
}

class CloneInstanceAsStaticObjectAction extends SceneTreePopupAction("Clone as new Static Object") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context match {
      case Some(r: ShapeModelInstance.MeshViewRepresentation) => true
      case _ => false
    }
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val mesh = context.get.asInstanceOf[ShapeModelInstance.MeshViewRepresentation]
      MeshView.createFromUnderlying(mesh.underlying, None, Some(s"${mesh.parent.name}-copy"))(mesh.parent.scene)
    }
  }
}

