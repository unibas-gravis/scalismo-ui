package usecases

import org.statismo.stk.ui.Scene
import java.io.File
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.OpenSceneTreeObjectAction
import org.statismo.stk.ui.SceneTreeObjectFactory
import org.statismo.stk.ui.StatismoFrame
import org.statismo.stk.ui.StatismoApp
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component
import org.statismo.stk.ui.ShapeModel
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.StaticMesh
import java.awt.Color
import org.statismo.stk.ui.DisplayableLandmarks
import org.statismo.stk.ui.swing.WorkspacePanel
import org.statismo.stk.ui.Landmarks
import scala.swing.Action
import org.statismo.stk.ui.ReferenceLandmarks
import scala.collection.mutable.ArrayBuffer
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.core.numerics.Integrator
import org.statismo.stk.core.registration.RegistrationConfiguration
import org.statismo.stk.core.statisticalmodel.StatisticalMeshModel
import org.statismo.stk.core.numerics.FixedPointsUniformMeshSampler3D
import org.statismo.stk.core.geometry.ThreeD
import org.statismo.stk.core.numerics.IntegratorConfiguration
import org.statismo.stk.core.numerics.LBFGSOptimizer
import org.statismo.stk.core.numerics.LBFGSOptimizerConfiguration
import org.statismo.stk.core.registration.MeanSquaresMetric3D
import org.statismo.stk.core.registration.RKHSNormRegularizer
import org.statismo.stk.core.registration.KernelTransformationSpace3D
import org.statismo.stk.core.registration.KernelTransformationSpaceConfiguration
import org.statismo.stk.core.image.DiscreteImage3D
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.filters.DistanceTransform
import org.statismo.stk.core.registration.Registration
import org.statismo.stk.core.mesh.Mesh
import org.statismo.stk.core.image.ContinuousScalarImage3D
import org.statismo.stk.ui.{ Mesh => UiMesh }
import org.statismo.stk.ui.ThreeDImage
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.image.Interpolation
import org.statismo.stk.core.io.ImageIO

class Varian(scene: Scene) extends StatismoFrame(scene) { self =>

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
    args foreach { arg =>
      scene.load(arg)
    }
  }

  this.toolbar.add(new Action("Compute Posterior") { def apply() = computeNewPosteriorModel(false) })
  this.toolbar.add(new Action("Start fitting") { def apply() = startFitting() })

  private var targetLm: Option[DisplayableLandmarks] = None
  private var modelLm: Option[ReferenceLandmarks] = None
  private var orgModel: Option[ShapeModel] = None
  private var lastModel: Option[ShapeModel] = None

  listenTo(scene)
  reactions += {
    case Scene.TreeTopologyChanged(s) => {
      if (!scene.models.children.isEmpty) {
        if (!orgModel.isDefined) {
          orgModel = Some(scene.models(0))
          //          orgModel.get.instances(0).meshRepresentation.color = Color.RED
          //          orgModel.get.instances(0).meshRepresentation.opacity = 0.75
          lastModel = Some(scene.models(0))
          modelLm = Some(orgModel.get.landmarks)
          listenTo(modelLm.get)
        }
      }
      if (!scene.statics.children.isEmpty) {
        if (!targetLm.isDefined) {
          targetLm = Some(scene.statics(0).landmarks)
          listenTo(targetLm.get)
        }
      }
    }
  }

  implicit val theScene = this.scene
  reactions += {
    case Landmarks.LandmarksChanged(lm) => {
      computeNewPosteriorModel(true)
    }
  }

  private def computeNewPosteriorModel(computeMeanOnly: Boolean): Unit = {
    if (modelLm.isDefined && targetLm.isDefined) {
      if (modelLm.get.children.length == targetLm.get.children.length) {
        val refLms = lastModel.get.landmarks.children.map(_.peer).toIndexedSeq
        val targetLms = targetLm.get.children.map(_.peer).toIndexedSeq
        val trainingData = refLms.zip(targetLms).map { case (refPt, tgtPt) => (refPt, tgtPt - refPt) }
        val newModel = if (!trainingData.isEmpty) {
          val posteriorModel = orgModel.get.peer.posterior(trainingData, 2, computeMeanOnly)
          val nm = ShapeModel(posteriorModel, orgModel)
          nm.landmarks.children.foreach { l => l.remove }
          lastModel.get.landmarks.children.foreach { lm => nm.landmarks.create(lm.peer) }
          nm
        } else {
          val nm = ShapeModel(orgModel.get.peer, orgModel)
          nm.landmarks.children.foreach(_.remove)
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
        transformationSpace = KernelTransformationSpace3D(KernelTransformationSpaceConfiguration[ThreeD](statmodel.gp, false)),
        regularizer = RKHSNormRegularizer,
        regularizationWeight = 0.01,
        initialParametersOrNone = None)
    }

    val config = fittingConfig(lastModel.get)
    val refDm = Mesh.meshToDistanceImage(lastModel.get.peer.mesh)

    val targetDm: ContinuousScalarImage3D = {
      scene.statics(0).representations(0) match {
        case m: UiMesh => Mesh.meshToDistanceImage(m.triangleMesh)
        case imgUi: ThreeDImage => {
          val img = imgUi.peer
          val timg: DiscreteScalarImage3D[Short] = img.map(v => if (v > 10) 1 else 0)
          //          ImageIO.writeImage(timg, new File("/tmp/t.nii"))
          //            ImageIO.writeImage(DistanceTransform.euclideanDistanceTransform(timg), new File("/tmp/dm.nii"))
          Interpolation.interpolate(DistanceTransform.euclideanDistanceTransform(timg), 1)

        }
      }
    }
    for (regstate <- Registration.iterations(config)(refDm, targetDm)) {
      println("regstate: " + regstate.optimizerState.value)
      lastModel.get.instances(0).coefficients = regstate.optimizerState.parameters.toArray
    }
  }

}

object Varian {
  def main(args: Array[String]): Unit = {
    StatismoApp(args, frame = { s: Scene => new Varian(s) })
  }
}

