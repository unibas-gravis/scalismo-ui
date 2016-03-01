package plugin

import scalismo.ui.view.{ ScalismoApplication, ScalismoFrame }

class SimpleViewer extends ScalismoFrame {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)
    statusBar.set("Hello World!")
  }
}

object SimpleViewer {
  def main(args: Array[String]): Unit = {
    ScalismoApplication(new SimpleViewer, args)
  }
}
