package org.statismo.stk.ui.swing.menu

import org.statismo.stk.ui.StatismoFrame
import scala.swing.{Action, CheckMenuItem, MenuItem, Menu}
import scala.swing.event.ButtonClicked
import org.statismo.stk.ui.swing.Console
import scala.ref.WeakReference

object ToolsMenu {
  val Name = "Tools"
}

class ToolsMenu(implicit app: StatismoFrame) extends Menu(ToolsMenu.Name) {
  contents += new ConsoleMenuItem
  contents += new GarbageCollectMenuItem
}

object ConsoleMenuItem {
  val Name = "Scala Console"
}

class ConsoleMenuItem(implicit app: StatismoFrame) extends CheckMenuItem(ConsoleMenuItem.Name) {
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