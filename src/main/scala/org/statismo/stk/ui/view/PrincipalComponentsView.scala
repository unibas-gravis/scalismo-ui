package org.statismo.stk.ui.view

import org.statismo.stk.ui.controller.PrincipalComponentsController

trait PrincipalComponentsView {
	def controller: PrincipalComponentsController
	def setCoefficients(coeffs: Seq[Float]): Unit
}