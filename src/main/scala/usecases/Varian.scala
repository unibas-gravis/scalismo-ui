package usecases

import scala.async.Async.async
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.runtime.universe.TypeTag.Short
import scala.swing.Action
import org.statismo.stk.core.filters.DistanceTransform
import org.statismo.stk.core.image.Interpolation
import org.statismo.stk.core.mesh.Mesh
import org.statismo.stk.core.numerics.FixedPointsUniformMeshSampler3D
import org.statismo.stk.core.numerics.Integrator
import org.statismo.stk.core.numerics.IntegratorConfiguration
import org.statismo.stk.core.numerics.LBFGSOptimizer
import org.statismo.stk.core.numerics.LBFGSOptimizerConfiguration
import org.statismo.stk.core.registration.KernelTransformationSpace3D
import org.statismo.stk.core.registration.KernelTransformationSpaceConfiguration
import org.statismo.stk.core.registration.MeanSquaresMetric3D
import org.statismo.stk.core.registration.RKHSNormRegularizer
import org.statismo.stk.core.registration.Registration
import org.statismo.stk.core.registration.RegistrationConfiguration
import org.statismo.stk.ui.{Mesh => UiMesh, _}
import org.statismo.stk.core.geometry.ThreeD
import scala.collection.mutable.ArrayBuffer
import org.statismo.stk.core.image.ContinuousScalarImage3D
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.registration.KernelTransformationSpace3D
import scala.Some
import org.statismo.stk.core.numerics.LBFGSOptimizer
import org.statismo.stk.core.registration.KernelTransformationSpaceConfiguration
import org.statismo.stk.core.numerics.LBFGSOptimizerConfiguration
import org.statismo.stk.core.registration.RegistrationConfiguration
import org.statismo.stk.core.numerics.Integrator
import org.statismo.stk.core.numerics.FixedPointsUniformMeshSampler3D
import org.statismo.stk.core.numerics.IntegratorConfiguration
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.image.ContinuousScalarImage3D
import org.statismo.stk.core.registration.MeanSquaresMetric3D

class Varian(scene: Scene) extends StatismoFrame(scene) {

  //  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneTreeObjectAction(loadMesh, "Open Mesh...", Seq(StaticMesh), false)))
  //  menuBar.fileMenu.contents.insert(0, new MenuItem(new OpenSceneTreeObjectAction(loadModel, "Open Statistical Shape Model...", Seq(ShapeModel), false)))

  //  def loadMesh(files: Seq[File], factories: Seq[SceneTreeObjectFactory[SceneTreeObject]]): Unit = {
  //    val x = scene.tryLoad(files.map(f => f.getAbsolutePath()).toList, factories)
  //    x foreach { o =>
  //      if (o.isSuccess) {
  //        val s = o.get.asInstanceOf[StaticMesh]
  //        meshLm = s.parent.parent.landmarks
  //        listenTo(meshLm)
  //        s.color = Color.GREEN
  //      }
  //    }
  //  }
  //
  //  def loadModel(files: Seq[File], factories: Seq[SceneTreeObjectFactory[SceneTreeObject]]): Unit = {
  //    files foreach { fn =>
  //      val o = ShapeModel(fn, 1)
  //      if (o.isSuccess) {
  //        orgModel = Some(o.get)
  //        lastModel = Some(o.get)
  //        o.get.instances(0).meshRepresentation.color = Color.RED
  //        modelLm = o.get.instances(0).landmarks
  //        listenTo(modelLm)
  //      }
  //    }
  //  }
  //
  override def startup(args: Array[String]): Unit = {
    super.startup(args)
    args foreach { scene.tryLoad(_) }
  }

  this.toolbar.add(new Action("Compute Posterior") { def apply() = computeNewPosteriorModel(computeMeanOnly = false) })
  this.toolbar.add(new Action("Start fitting") { def apply() = startFitting() })

  private var targetLm: Option[VisualizableLandmarks] = None
  private var modelLm: Option[ReferenceLandmarks] = None
  private var orgModel: Option[ShapeModel] = None
  private var lastModel: Option[ShapeModel] = None

  listenTo(scene)
  reactions += {
    case Scene.TreeTopologyChanged(s) =>
      if (!scene.shapeModels.children.isEmpty) {
        if (!orgModel.isDefined) {
          orgModel = Some(scene.shapeModels(0))
          lastModel = Some(orgModel.get)
          modelLm = Some(orgModel.get.landmarks)
          listenTo(modelLm.get)
        }
      }
      if (!scene.staticObjects.children.isEmpty) {
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
      if (modelLm.get.children.length == targetLm.get.children.length) {
        val refLms = lastModel.get.landmarks.children.map(_.point).toIndexedSeq
        val targetLms = targetLm.get.children.map(_.point).toIndexedSeq
        val trainingData = refLms.zip(targetLms).map { case (refPt, tgtPt) => (refPt, tgtPt - refPt) }
        val newModel = if (!trainingData.isEmpty) {
          val posteriorModel = orgModel.get.peer.posterior(trainingData, 2, computeMeanOnly)
          val nm = ShapeModel(posteriorModel, orgModel)
          nm.landmarks.children.foreach { l => l.remove() }
          lastModel.get.landmarks.children.foreach { lm => nm.landmarks.create(lm.point) }
          nm
        } else {
          val nm = ShapeModel(orgModel.get.peer, orgModel)
          nm.landmarks.children.foreach(_.remove())
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
      val sampler = FixedPointsUniformMeshSampler3D(statmodel.peer.mesh, 1000, seed = 42)
      val integr = Integrator[ThreeD](IntegratorConfiguration(sampler))
      RegistrationConfiguration[ThreeD](
        optimizer = LBFGSOptimizer(LBFGSOptimizerConfiguration(numIterations = 10, 5, 0.001)),
        //optimizer = GradientDescentOptimizer(GradientDescentConfiguration(numIterations = 40, stepLength = 1.0)),
        integrator = integr,
        metric = MeanSquaresMetric3D(integr),
        transformationSpace = KernelTransformationSpace3D(KernelTransformationSpaceConfiguration[ThreeD](statmodel.gaussianProcess, withValueCaching = false)),
        regularizer = RKHSNormRegularizer,
        regularizationWeight = 0.01,
        initialParametersOrNone = None)
    }

    val config = fittingConfig(lastModel.get)
    val refDm = Mesh.meshToDistanceImage(lastModel.get.peer.mesh)

    val targetDm: ContinuousScalarImage3D = {
      scene.staticObjects(0).representations(0) match {
        case m: UiMesh => Mesh.meshToDistanceImage(m.peer)
        case imgUi: Image3D[_] =>
          val img = imgUi.peer.asInstanceOf[DiscreteScalarImage3D[Short]]
          val timg: DiscreteScalarImage3D[Short] = img.map(v => if (v > 10) 1 else 0)
          //          ImageIO.writeImage(timg, new File("/tmp/t.nii"))
          //            ImageIO.writeImage(DistanceTransform.euclideanDistanceTransform(timg), new File("/tmp/dm.nii"))
          Interpolation.interpolate(DistanceTransform.euclideanDistanceTransform(timg), 1)
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
    StatismoApp(args, frame = { s: Scene => new Varian(s) })
  }
}

