package usecases

import org.statismo.stk.core.geometry._3D
import org.statismo.stk.core.image.filter.DiscreteImageFilter
import org.statismo.stk.core.image.{DifferentiableScalarImage, DiscreteScalarImage}
import org.statismo.stk.core.mesh.Mesh
import org.statismo.stk.core.numerics.{FixedPointsUniformMeshSampler3D, Integrator, LBFGSOptimizer}
import org.statismo.stk.core.registration.{GaussianProcessTransformationSpace, MeanSquaresMetric3D, RKHSNormRegularizer, Registration, RegistrationConfiguration}
import org.statismo.stk.ui.{Mesh => UiMesh, _}

import scala.collection.mutable.ArrayBuffer
import scala.swing.Action

class Varian(scene: Scene) extends StatismoFrame(scene) {

  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    args foreach {
      scene.tryLoad(_)
    }
  }

  this.toolbar.add(new Action("Compute Posterior") {
    def apply() = computeNewPosteriorModel(computeMeanOnly = false)
  })
  this.toolbar.add(new Action("Start fitting") {
    def apply() = startFitting()
  })

  private var targetLm: Option[VisualizableLandmarks] = None
  private var modelLm: Option[ReferenceLandmarks] = None
  private var orgModel: Option[ShapeModel] = None
  private var lastModel: Option[ShapeModel] = None

  listenTo(scene)
  reactions += {
    case Scene.TreeTopologyChanged(s) =>
      if (scene.shapeModels.nonEmpty) {
        if (!orgModel.isDefined) {
          orgModel = Some(scene.shapeModels(0))
          lastModel = Some(orgModel.get)
          modelLm = Some(orgModel.get.landmarks)
          listenTo(modelLm.get)
        }
      }
      if (scene.staticObjects.nonEmpty) {
        if (!targetLm.isDefined) {
          targetLm = Some(scene.staticObjects(0).landmarks)
          listenTo(targetLm.get)
        }
      }
  }

  implicit val theScene = this.scene
  reactions += {
    case Landmarks.LandmarksChanged(lm) =>
      computeNewPosteriorModel(computeMeanOnly = true)
  }

  private def computeNewPosteriorModel(computeMeanOnly: Boolean): Unit = {
    if (modelLm.isDefined && targetLm.isDefined) {
      if (modelLm.get.length == targetLm.get.length) {
        val refLms = lastModel.get.landmarks.map(_.point).toIndexedSeq
        val targetLms = targetLm.get.map(_.point).toIndexedSeq
        val trainingData = refLms.zip(targetLms).map { case (refPt, tgtPt) => (lastModel.get.peer.referenceMesh.findClosestPoint(refPt)._2, tgtPt)}
        val newModel = if (trainingData.nonEmpty) {
          val posteriorModel = orgModel.get.peer.posterior(trainingData, 2 /*, computeMeanOnly*/)
          val nm = ShapeModel.createFromPeer(posteriorModel, orgModel.get)
          nm.landmarks.foreach { l => l.remove()}
          lastModel.get.landmarks.foreach { lm => nm.landmarks.create(lm.point, Some(lm.name), lm.uncertainty)}
          nm
        } else {
          val nm = ShapeModel.createFromPeer(orgModel.get.peer, orgModel.get)
          nm.landmarks.foreach(_.remove())
          nm
        }

        lastModel.get.remove()
        lastModel = Some(newModel)
        deafTo(modelLm.get)
        modelLm = Some(newModel.landmarks)
        listenTo(modelLm.get)
      }
    }
  }

  import scala.async.Async.async
  import scala.concurrent.ExecutionContext.Implicits.global

  private def startFitting(): Unit = async {
    val coeffs: ArrayBuffer[Float] = new ArrayBuffer()
    coeffs ++= lastModel.get.instances(0).coefficients

    def fittingConfig(statmodel: ShapeModel) = {
      val sampler = FixedPointsUniformMeshSampler3D(statmodel.peer.referenceMesh, 1000, seed = 42)
      val integr = Integrator[_3D](sampler)
      RegistrationConfiguration[_3D](
        optimizer = LBFGSOptimizer(numIterations = 10, 5, 0.001),
        //optimizer = GradientDescentOptimizer(GradientDescentConfiguration(numIterations = 40, stepLength = 1.0)),
        integrator = integr,
        metric = MeanSquaresMetric3D(integr),
        transformationSpace = GaussianProcessTransformationSpace(statmodel.peer.gp.interpolate()),
        regularizer = RKHSNormRegularizer,
        regularizationWeight = 0.01)
    }

    val config = fittingConfig(lastModel.get)
    val refDm = Mesh.meshToDistanceImage(lastModel.get.peer.referenceMesh)

    val targetDm: DifferentiableScalarImage[_3D] = {
      scene.staticObjects(0).representations(0) match {
        case m: UiMesh => Mesh.meshToDistanceImage(m.peer)
        case imgUi: Image3D[_] =>
          val img = imgUi.peer.asInstanceOf[DiscreteScalarImage[_3D, Short]]
          val timg: DiscreteScalarImage[_3D, Short] = img.map(v => if (v > 10) 1 else 0)
          //          ImageIO.writeImage(timg, new File("/tmp/t.nii"))
          //            ImageIO.writeImage(DistanceTransform.euclideanDistanceTransform(timg), new File("/tmp/dm.nii"))
          val dt = DiscreteImageFilter.distanceTransform(timg)
          dt.interpolate(1)
      }
    }
    for (regstate <- Registration.iterations(config)(refDm, targetDm)) {
      println("regstate: " + regstate.optimizerState.value)
      lastModel.get.instances(0).coefficients = regstate.optimizerState.parameters.toArray.toIndexedSeq
    }
  }
}

object Varian {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = { s: Scene => new Varian(s)})
  }
}

