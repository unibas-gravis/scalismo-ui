package scalismo.ui.api

import scalismo.ui.control.interactor.{ DefaultInteractor, Interactor }
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.ScalismoFrame

class ScalismoUI(title : String) extends SimpleAPI {

  private[ui] val frame = EdtUtil.onEdtWait {
    val frame = new ScalismoFrame()

    frame.setup(Array[String]())
    frame.pack()
    frame.visible = true

    frame.title = title;
    frame
  }

  override protected[api] val scene = frame.scene

}

object ScalismoUI {

  def apply(title : String = ""): ScalismoUI = {
    scalismo.initialize()
    new ScalismoUI(title)
  }
}

