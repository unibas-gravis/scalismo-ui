package org.statismo.stk.ui.swing.actions

import org.statismo.stk.ui.Saveable
import org.statismo.stk.ui.SceneTreeObject
import java.io.File

class SaveAction extends SceneTreePopupAction("Save to File...") {
	def isContextSupported(context: Option[SceneTreeObject]) = {
	  context.isDefined && context.get.isInstanceOf[Saveable]
	}
	
	def apply(context: Option[SceneTreeObject]) = {
	  if (isContextSupported(context)) {
	    context.get.asInstanceOf[Saveable].saveToFile(new File("/tmp/tmp.tmp"))
	  }
	}
}