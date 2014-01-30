package org.statismo.stk.ui.controller

import org.statismo.stk.ui.StatModel
import breeze.stats.distributions.Gaussian
import scala.swing.Reactor
import org.statismo.stk.ui.view.PrincipalComponentsView
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import org.statismo.stk.ui.SceneObject

class PrincipalComponentsController(val model: StatModel, val min: Float = -3.0f, val max: Float = 3.0f) extends Reactor {
	require(min < max)
	listenTo(model)
	
	val views: Buffer[PrincipalComponentsView] = new ListBuffer[PrincipalComponentsView]
	
	reactions += {
	  case SceneObject.GeometryChanged => updateViews
	}
	
	def getCoefficients: Seq[Float] = {
	  model.coefficients
	}
	
	def setCoefficient(index: Int, value: Float) = {
	  // the map is a simple way to ensure that a new object is created. Otherwise, we may simply be operating on the original object.
	  val coeffs = (model.coefficients.map(f => f)).toArray
	  if (index >= 0 && index < coeffs.length) {
		  coeffs(index) = sanitize(value)
		  model.coefficients = coeffs
	  }
	}
	
	def sanitize(value: Float) = {
	  Math.min(max, Math.max(value, min))
	}
	
	def updateViews : Unit = {
	  views foreach (_.setCoefficients(model.coefficients))
	}
	
	def reset(): Unit = {
	  model.coefficients = IndexedSeq.fill(model.coefficients.length)(0.0f) 
	}
	
	def randomize(): Unit = {
	  val coeffs = model.coefficients.map { v =>
		  sanitize(Gaussian(0, 1).draw().toFloat)
	  }
	  model.coefficients = coeffs
	}
}