package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.Reactor
import scala.swing.event.Event

case class ViewportChanged extends Event

class Viewport(val scene: Scene) extends Publisher {
	listenTo(scene)
	
	reactions += {
	  case SceneChanged() => {
	    publish(ViewportChanged())
	  }
	}
}

trait ViewportAdapter extends Reactor {
  val viewport: Viewport
  listenTo(viewport)
  
  reactions += {
    case ViewportChanged() => {
      reloadViewport
    }
  }
  
  def reloadViewport : Unit
}