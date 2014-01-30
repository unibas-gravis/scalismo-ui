package standalonetools

import org.statismo.stk.core.io.StatismoIO
import scala.util.Failure
import scala.util.Success
import java.io.File
import org.statismo.stk.core.statisticalmodel.StatisticalMeshModel
import scala.swing.SimpleSwingApplication
import org.statismo.stk.core.utils.MeshConversion
import breeze.linalg.DenseVector
import vtk.vtkPanel
import scala.swing.Slider
import javax.swing.JPanel
import java.awt.BorderLayout
import scala.swing.BoxPanel
import scala.swing.Orientation
import scala.swing.Button
import vtk.vtkPolyDataMapper
import vtk.vtkActor
import org.statismo.stk.core.statisticalmodel.SpecializedLowRankGaussianProcess
import org.statismo.stk.core.geometry.ThreeD
import vtk.vtkFloatArray
import scala.swing.MainFrame
import java.awt.Dimension
import scala.swing.event.ValueChanged
import scala.swing.event.ButtonClicked
import javax.swing.WindowConstants.{ DISPOSE_ON_CLOSE, DO_NOTHING_ON_CLOSE }
import scala.swing.Component
import org.statismo.stk.core.mesh.TriangleMesh
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Action
import scala.swing.FileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import org.statismo.stk.core.statisticalmodel.LowRankGaussianProcess
import vtk.vtkPolyData
import scala.swing.Reactor
import scala.swing.Menu
import scala.swing.Label
import scala.swing.Dialog

object EDT {
  def onEDT[A](prog: => Unit): Unit =
    if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
      javax.swing.SwingUtilities.invokeLater {
        new Runnable() {
          def run() = prog
        }
      }
    } else prog //val actors = ListBuffer[vtkActor]()

}

class StatismoModelViewer(val heightX: Int, val width: Int) extends MainFrame with Reactor { self =>

  class SliderPanel(sliders: Seq[Slider]) extends BoxPanel(Orientation.Vertical) {
    contents += new Label("Principal components (+/- 5 sigma)\n")
    for ((slider, num) <- sliders.zipWithIndex) {
      val sliderEntry = new BoxPanel(Orientation.Horizontal) {
        contents += new Label(s"PC ${"%02d".format(num)}: ")
        contents += slider
      }

      contents += sliderEntry
      contents += resetButton
      contents += randomButton

    }

  }

  case class ModelData(statmodel: StatisticalMeshModel) {
    val gp: SpecializedLowRankGaussianProcess[ThreeD] = {
      statmodel.gp match {
        case specializedGP: SpecializedLowRankGaussianProcess[ThreeD] => specializedGP
        case gp => gp.specializeForPoints(refMesh.points.force)
      }
    }
    val refMesh: TriangleMesh = statmodel.mesh
    val refMeshAsPolyData: vtkPolyData = MeshConversion.meshToVTKPolyData(refMesh)
    def numberOfPoints = refMesh.numberOfPoints

    var coeffs = DenseVector.zeros[Float](gp.rank)

  }

  private var _modelData: Option[ModelData] = None
  def modelData = _modelData
  def modelData_=(mdOption: Option[ModelData]): Unit = {
    mdOption.map { md =>
      _modelData = Some(md)
      mapper.SetInputData(md.refMeshAsPolyData)
      sliders = createSliders(md.gp.rank)
    }
  }

  // Setup VTK rendering panel, this also loads VTK native libraries
  var renWin = new vtkPanel
  var sliders: IndexedSeq[Slider] = IndexedSeq[Slider]()

  def createSliders(numSliders: Int): IndexedSeq[Slider] = {
    for (i <- 0 until numSliders) yield {
      val slider = new Slider()
      slider.min = -5
      slider.max = 5
      slider.snapToTicks = true
      slider.majorTickSpacing = 1
      slider.name = i.toString
      slider.value = 0
      slider
    }
  }

  val resetButton = new Button()
  resetButton.text = "reset"
  resetButton.name = "reset"

  val randomButton = new Button()
  randomButton.text = "random"
  randomButton.name = "random"
  val sliderPanel = new SliderPanel(sliders)
  // Create wrapper to integrate vtkPanel with Scala's Swing API
  val renWinPanel = new Component {
    override lazy val peer = new JPanel(new BorderLayout())
    peer.add(renWin)
  }

  var mapper = new vtkPolyDataMapper

  var actor = new vtkActor
  actor.SetMapper(mapper)

  EDT.onEDT {
    renWin.GetRenderer.AddActor(actor)
    renWin.GetRenderer.ResetCamera
  }

  def resetCameraAndRender(): Unit = {
    EDT.onEDT {
      renWin.GetRenderer().ResetCamera()
      renWin.Render()
    }
  }

  def updateMesh(coeffs: DenseVector[Float]): Unit = {
    modelData match {
      case Some(md) => {
        val ptdefs = md.gp.instanceAtPoints(coeffs)
        val newptseq = for ((pt, df) <- ptdefs) yield pt + df
        val newMesh = MeshConversion.meshToVTKPolyData(new TriangleMesh(newptseq, md.refMesh.cells), Some(md.refMeshAsPolyData))
        mapper.SetInputData(newMesh)
        renWin.Render()
      }
      case None => {}
    }
  }

  def updateSliders(coeffs: DenseVector[Float]): Unit = {
    for ((slider, i) <- sliders.zipWithIndex) slider.value = coeffs(i).toInt
  }

  title = "Statimso Shape Model Viewer"

  peer.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
  
  override def closeOperation() {
    println("closing")
    dispose()
  }

  contents = new BoxPanel(Orientation.Horizontal) {
    contents += renWinPanel
    contents += sliderPanel
  }

  EDT.onEDT {
    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(Action("Open Model") {
          val modelChooser = new FileChooser() {
            title = "Choose statistical model"
            //fileFilter = new FileNameExtensionFilter("statistical models", "h5", "hdf5")
          }

          modelChooser.showOpenDialog(this)
          val modelFilename = modelChooser.selectedFile
          if (modelFilename != null) {
            // TODO error handling
            modelData = StatismoIO.readStatismoMeshModel(modelFilename).toOption.map(model => ModelData(model))
            resetCameraAndRender()
          }
        })
        contents += new MenuItem(Action("Quit") {
          EDT.onEDT(dispose())
          
        })

      }
      contents += new Menu("Help") {
        contents += new MenuItem(Action("About") {
          Dialog.showMessage(this,
            "Statismo model viewer, Version 0.1\n\n" +
              "Copyright 2014, University of Basel. \n \n " +
              "Authors: \n Marcel Luethi \n Ghazi Bouabene\n Christoph Langguth", title = "About")
        })
      }
    }

  }

  for (slider <- sliders) {
    listenTo(slider)
  }
  listenTo(resetButton)
  listenTo(randomButton)
  reactions += {
    case ValueChanged(s) => {
      val slider = s.asInstanceOf[Slider]
      modelData.map { md =>
        md.coeffs(slider.name.toInt) = slider.value.toFloat
        updateMesh(md.coeffs)
      }
    }
    case ButtonClicked(b) => {
      b.name match {
        case "reset" => {
          modelData.map { md =>
            md.coeffs.foreach(_ => 0.0)
            updateSliders(md.coeffs)
            updateMesh(md.coeffs)
          }
        }
        case "random" => {
          modelData.map { md =>
            val gaussian = breeze.stats.distributions.Gaussian(0, 1)
            md.coeffs.foreach(_ => gaussian.draw.toFloat)
            updateSliders(md.coeffs)
            updateMesh(md.coeffs)
          }
        }
      }

    }

  }

  this.maximize
}

object StatismoModelViewer extends SimpleSwingApplication {

  org.statismo.stk.core.initialize()

  def top = new StatismoModelViewer(1024, 768)

}
