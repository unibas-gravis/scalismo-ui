package org.statismo.stk.ui.swing.actions

import org.statismo.stk.ui.Saveable
import org.statismo.stk.ui.SceneTreeObject
import java.io.File
import scala.util.Try
import org.statismo.stk.ui.Loadable
import org.statismo.stk.ui.ShapeModel
import org.statismo.stk.ui.ShapeModels
import org.statismo.stk.ui.ShapeModelInstances
import scala.util.Success

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
    context.isDefined && context.get.isInstanceOf[ShapeModel]  && context.get.asInstanceOf[ShapeModel].landmarks.isCurrentlySaveable
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
        ShapeModel(file)(shapes.scene).map(ok => Success())
      }
      new LoadAction(doLoad, ShapeModel).apply
    }
  }
}

class CreateShapeModelInstanceAction extends SceneTreePopupAction("Create new Instance") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[ShapeModelInstances]
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val instances = context.get.asInstanceOf[ShapeModelInstances]
      instances.create()
    }
  }
}