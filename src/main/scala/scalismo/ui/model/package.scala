package scalismo.ui

import scalismo.geometry.{ Point, _3D }

package object model {
  type PointCloud = IndexedSeq[Point[_3D]]
  type PointTransformation = Point[_3D] => Point[_3D]

}
