package scalismo.ui.api

import scalismo.ui.control.interactor.{ DefaultInteractor, Interactor }
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.{ ScalismoFrame, ScalismoLookAndFeel }

class ScalismoUI(title: String) extends SimpleAPI {

  private[ui] val fr = EdtUtil.onEdtWait {
    val frame = new ScalismoFrame()

    frame.setup(Array[String]())
    frame.pack()
    frame.visible = true

    frame.title = title;
    frame
  }

  override protected[api] val scene = fr.scene
  override protected[api] val frame = fr
}

object ScalismoUI {

  def apply(title: String = ""): ScalismoUI = {
    scalismo.initialize()
    ScalismoLookAndFeel.initializeWith(ScalismoLookAndFeel.DefaultLookAndFeelClassName)
    new ScalismoUI(title)
  }
}

