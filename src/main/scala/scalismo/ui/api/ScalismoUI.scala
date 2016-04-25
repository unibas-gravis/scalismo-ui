package scalismo.ui.api

import scalismo.ui.control.interactor.{ DefaultInteractor, Interactor }
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.ScalismoFrame

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

