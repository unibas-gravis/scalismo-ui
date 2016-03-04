package scalismo.ui.view.action

import javax.swing.UIManager

import scalismo.ui.view.{ HighDpi, ScalismoFrame }

import scala.swing.{ Action, Dialog }
import scala.util.Try

class AboutDialogAction(name: String)(implicit val frame: ScalismoFrame) extends Action(name) {

  /**
   * This is essentially a workaround for IntelliJ Idea not liking
   * auto-generated sources. (I.e. Idea continues to bitch about
   * not finding scalismo.ui.BuildInfo, thus making it impossible
   * to compile or start the program from the UI).
   *
   * Using runtime reflection avoids this.
   */
  private object Proxy {

    import scala.reflect.runtime.universe

    lazy val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    lazy val objectName = "scalismo.ui.BuildInfo$"

    def proxy(fieldName: String): String = Try {
      val moduleSymbol = runtimeMirror.moduleSymbol(
        Class.forName(objectName))

      val targetMethod = moduleSymbol.typeSignature
        .members
        .filter(x => x.isMethod && x.name.toString == fieldName)
        .head
        .asMethod

      runtimeMirror.reflect(runtimeMirror.reflectModule(moduleSymbol).instance)
        .reflectMethod(targetMethod)().toString
    }.getOrElse("???")

    // proxied fields
    def version: String = proxy("version")

    def scalaVersion: String = proxy("scalaVersion")
  }

  def apply() = {
    val icon = UIManager.getIcon("OptionPane.informationIcon")
    val scaledIcon = HighDpi.scaleIcon(icon, HighDpi.scale(icon.getIconWidth), HighDpi.scale(icon.getIconHeight))
    Dialog.showMessage(frame.contents.head,
      s"""
Scalismo Viewer version: ${Proxy.version}
Scalismo version: ${scalismo.BuildInfo.version}
Scala version: ${Proxy.scalaVersion}

Copyright 2014-2016, University of Basel.

Authors:
 Ghazi Bouabene
 Christoph Langguth
 Marcel Lüthi

Feedback is very welcome!""", title = name, icon = scaledIcon)
  }
}
