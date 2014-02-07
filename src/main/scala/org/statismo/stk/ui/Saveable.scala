package org.statismo.stk.ui

import java.io.File
import scala.util.Try

trait Saveable {
	def saveToFile(file: File): Try[Unit]
}