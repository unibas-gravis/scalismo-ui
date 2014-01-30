package org.statismo.stk.ui

import java.io.File
import org.statismo.stk.core.io.StatismoIO
import org.statismo.stk.core.statisticalmodel.StatisticalMeshModel
import scala.util.Try
import org.statismo.stk.core.mesh.TriangleMesh
import scala.swing.Publisher
import scala.swing.event.Event
import org.statismo.stk.core.statisticalmodel.SpecializedLowRankGaussianProcess
import org.statismo.stk.core.geometry.ThreeD
import breeze.linalg.DenseVector

object StatModel extends Loadable[StatModel] {
  def apply(file: File): Try[StatModel] = {
    for {
      raw <- StatismoIO.readStatismoMeshModel(file)
    } yield new StatModel(raw, file.getName())
  }
  
  val description = "Statistical Shape Model"
  val fileExtensions = Seq[String]("h5")
}

class StatModel(val rawModel: StatisticalMeshModel, val displayName: String) extends Surface {
  lazy val refMesh = rawModel.mesh
  var mesh = refMesh 
  lazy val gp: SpecializedLowRankGaussianProcess[ThreeD] = {
    rawModel.gp match {
      case specializedGP: SpecializedLowRankGaussianProcess[ThreeD] => specializedGP
      case gp => gp.specializeForPoints(refMesh.points.force)
    }
  }

  private var coeffs: IndexedSeq[Float] = IndexedSeq.fill(gp.rank)(0.0f)
  
  def coefficients : IndexedSeq[Float] = {coeffs}
  def coefficients_=(newCoeffs: IndexedSeq[Float]) = {
    if (coeffs != newCoeffs) {
	    val vector = DenseVector[Float](newCoeffs.toArray)
		val ptdefs = gp.instanceAtPoints(vector)
	    coeffs = newCoeffs
	    val newptseq = for ((pt, df) <- ptdefs) yield pt + df
	    mesh = new TriangleMesh(newptseq, refMesh.cells)
	    publish(SceneObject.GeometryChanged)
    }
  }
  
}
