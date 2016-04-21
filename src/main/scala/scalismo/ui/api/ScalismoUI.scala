package scalismo.ui.api

import java.awt.Color
import java.io.File

import scalismo.common.{ DiscreteVectorField, UnstructuredPointsDomain, ScalarArray, DiscreteScalarField }
import scalismo.geometry.{ Landmark, Point3D, Point, _3D }
import scalismo.io.{ ActiveShapeModelIO, ImageIO, StatismoIO, MeshIO }
import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.{ Interactor, DefaultInteractor }
import scalismo.ui.model.StatusMessage
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.ScalismoFrame

import scala.swing.{ Component, Button }

class ScalismoUI() extends SimpleAPI {

  private[ui] val frame = EdtUtil.onEdtWait {
    val frame = new ScalismoFrame()
    frame.setup(Array[String]())
    frame.pack()
    frame.visible = true
    frame
  }

  override protected[api] val scene = frame.scene

}

object ScalismoUI {

  def apply(interactor: Interactor = new DefaultInteractor {}): ScalismoUI = {
    scalismo.initialize()
    new ScalismoUI()
  }
}

