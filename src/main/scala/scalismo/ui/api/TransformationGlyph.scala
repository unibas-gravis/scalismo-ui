package scalismo.ui.api

import scalismo.common.UnstructuredPointsDomain3D
import scalismo.geometry.{_3D, Point}

/**
  * A dummy class used only in the api to be able to show the transformation glyphs using a uniform syntax
  */
case class TransformationGlyph(points : IndexedSeq[Point[_3D]])

object TransformationGlyph {
  def apply(domain : UnstructuredPointsDomain3D) : TransformationGlyph = new TransformationGlyph(domain.pointSequence)
}
