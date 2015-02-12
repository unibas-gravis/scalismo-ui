package scalismo.ui.swing.menu

import scalismo.ui.swing.{ Console, ScalismoFrame }

import scala.ref.WeakReference
import scala.swing.event.ButtonClicked
import scala.swing.{ Action, CheckMenuItem, Menu, MenuItem }

object ToolsMenu {
  val Name = "Tools"
}

class ToolsMenu(implicit app: ScalismoFrame) extends Menu(ToolsMenu.Name) {
  contents += new ConsoleMenuItem
  contents += new GarbageCollectMenuItem
}

object ConsoleMenuItem {
  val Name = "Scala Console"
}

class ConsoleMenuItem(implicit app: ScalismoFrame) extends CheckMenuItem(ConsoleMenuItem.Name) {
  selected = app.console.visible
  listenTo(app.console)
  reactions += {
    case ButtonClicked(b) => app.console.visible = selected
    case Console.VisibilityChanged(c) => selected = c.visible
  }
}

object GarbageCollectMenuItem {
  val Name = "Force Garbage Collection"
}

class GarbageCollectMenuItem extends MenuItem(new GarbageCollectAction(GarbageCollectMenuItem.Name))

class GarbageCollectAction(name: String) extends Action(name) {
  override def apply(): Unit = {
    val start = System.currentTimeMillis()
    var dummy = new Object
    val indicator = new WeakReference(dummy)
    var goOn = true
    var count = 0
    while (goOn) {
      count = count + 1
      dummy = null
      System.gc()
      goOn = indicator.get.isDefined
    }
    val duration = System.currentTimeMillis() - start
    println(s"Garbage collection done after $count iterations ($duration ms)")
  }
}