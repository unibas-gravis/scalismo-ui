package scalismo.ui.view.action.popup

import java.io.File

import scalismo.io.StatismoIO
import scalismo.ui.model.{ GroupNode, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.util.{ FileIoMetadata, FileUtil }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.LoadAction

import scala.util.{ Failure, Success, Try }

object LoadStatisticalShapeModelAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[GroupNode](context).map(n => new LoadStatisticalShapeModelAction(n)).toList
  }
}

class LoadStatisticalShapeModelAction(group: GroupNode)(implicit frame: ScalismoFrame) extends PopupAction(s"Load ${FileIoMetadata.StatisticalShapeModel.description} ...", BundledIcon.Load) {
  def load(file: File): Try[Unit] = {
    StatismoIO.readStatismoMeshModel(file) match {
      case Failure(ex) => Failure(ex)
      case Success(model) =>
        val basename = FileUtil.basename(file)
        group.addStatisticalMeshModel(model, basename)
        Success(())
    }
  }

  override def apply(): Unit = {
    new LoadAction(load, FileIoMetadata.StatisticalShapeModel).apply()
  }
}
