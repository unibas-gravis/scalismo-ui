package org.statismo.stk.ui

import java.io.File
import scala.util.Try

trait Loadable {
    def loadableMetadata: FileIoMetadata
	def loadFromFile(file: File): Try[Unit]
    def isCurrentlyLoadable: Boolean = true
}