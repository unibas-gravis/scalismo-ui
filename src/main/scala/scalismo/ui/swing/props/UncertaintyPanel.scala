package scalismo.ui.swing.props

import java.awt.Color
import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.border.{ Border, LineBorder, TitledBorder }
import javax.swing.event.{ DocumentEvent, DocumentListener }

import scalismo.geometry.{ SquareMatrix, Vector, _3D }
import scalismo.ui._
import scalismo.ui.swing.util.EdtSlider

import scala.swing._
import scala.swing.event.ValueChanged
import scala.util.Try

class UncertaintyPanel extends BorderPanel with PropertyPanel {
  override val description = "Uncertainty"

  private lazy val Unset = "(unset)"

  private var target: Option[HasUncertainty[_]] = None

  val (x, y, z, rotationValue) = {
    def l(t: TextField, f: Boolean => Unit) = new DocumentListener {
      override def insertUpdate(e: DocumentEvent): Unit = validateTextInput(t, f)

      override def changedUpdate(e: DocumentEvent): Unit = validateTextInput(t, f)

      override def removeUpdate(e: DocumentEvent): Unit = validateTextInput(t, f)
    }

    def c(f: Boolean => Unit) = new TextField() {
      inputVerifier = { c => validateTextInput(c.asInstanceOf[TextField], f) }
      peer.getDocument.addDocumentListener(l(this, f))
    }
    (c(onStddevInput), c(onStddevInput), c(onStddevInput), c(onRotationInput))
  }

  val (dx, dy, dz, px, py, pz) = {
    def c = new Label(Unset)
    (c, c, c, c, c, c)
  }

  private var stddev: Option[Vector[_3D]] = None
  private var defaultStddev: Option[Vector[_3D]] = None
  private var previousStddev: Option[Vector[_3D]] = None

  private var originalRotationMatrix: Option[SquareMatrix[_3D]] = None
  private var previousRotationMatrix: Option[SquareMatrix[_3D]] = None
  private var rotationMatrix: Option[SquareMatrix[_3D]] = None

  val set = new Button(new Action("Apply") {
    override def apply(): Unit = applyChanges(updatePreviousStdDev = true)
  })

  val reset = new Button(new Action("Reset (undo)") {
    override def apply(): Unit = {
      stddev = previousStddev
      updateStddev()
      applyChanges(updatePreviousStdDev = false)
    }
  })

  val toDefault = new Button(new Action("Save as default") {
    override def apply(): Unit = {
      stddev foreach (s => Uncertainty.defaultStdDevs3D = s)
    }
  })

  val fromDefault = new Button(new Action("Load from default") {
    override def apply(): Unit = {
      stddev = defaultStddev
      updateStddev()
      applyChanges(updatePreviousStdDev = false)
    }
  })

  val previousLabel = new Label("Previous:")
  val defaultLabel = new Label("Default:")

  val rotations = new FlowPanel()
  val stddevs = new GridPanel(0, 3)
  stddevs.contents ++= Seq(x, y, z)
  stddevs.contents ++= Seq(set, reset, previousLabel)
  stddevs.contents ++= Seq(px, py, pz)
  stddevs.contents ++= Seq(toDefault, fromDefault, defaultLabel)
  stddevs.contents ++= Seq(dx, dy, dz)

  val rotationLabel = Array.fill(3, 3)(new Label("X") {
    border = new LineBorder(Color.BLACK)
    peer.getInsets.set(5, 5, 5, 5)
  })

  val rotationReset = new Button(new Action("Reset") {
    override def apply(): Unit = {
      previousRotationMatrix = originalRotationMatrix
      rotationMatrix = previousRotationMatrix
      rotationSlider.value = 0
      applyRotation()
    }
  })

  val rotationSlider = new EdtSlider {
    min = -180
    max = 180

    reactions += {
      case ValueChanged(_) =>
        rotationValue.text = this.value.toString
    }
  }

  val (rotationFirst, rotationSecond, rotationThird) = {
    val group = new ButtonGroup()
    val listener = new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        previousRotationMatrix = rotationMatrix
        rotationSlider.value = 0
      }
    }
    def b(text: String) = {
      val b = new RadioButton(text)
      group.buttons += b
      b.peer.addActionListener(listener)
      b
    }
    (b("first"), b("second"), b("third"))
  }
  rotationFirst.selected = true

  // layout for the UI
  {
    val stddevsPanel = new BorderPanel {
      border = new TitledBorder(null, "Standard deviations (mm)", TitledBorder.LEADING, 0, null, null)
      layout(stddevs) = BorderPanel.Position.Center
    }
    val rotationsPanel = new BorderPanel {
      layoutManager.setHgap(20)
      border = new TitledBorder(null, "Rotation", TitledBorder.LEADING, 0, null, null)
      val matrix = new GridPanel(3, 3) {
        hGap = 0
        vGap = 0
        contents ++= rotationLabel.flatten.toSeq
        border = new LineBorder(Color.BLACK)
      }

      val lines = new BoxPanel(Orientation.Vertical)
      lines.contents += new BorderPanel {
        layout(new Label("Rotate around axis:")) = BorderPanel.Position.West
      }
      lines.contents += new GridPanel(1, 3) {
        contents ++= Seq(rotationFirst, rotationSecond, rotationThird)
      }
      lines.contents += new BorderPanel {
        layout(rotationSlider) = BorderPanel.Position.Center
        layout(new Label(rotationSlider.min.toString)) = BorderPanel.Position.West
        layout(new Label(rotationSlider.max.toString)) = BorderPanel.Position.East
      }
      lines.contents += new BorderPanel {
        val west = new GridPanel(0, 4) {
          contents ++= Seq(rotationValue, new Label("degrees"), rotationReset)
        }
        layout(west) = BorderPanel.Position.Center
      }
      layout(matrix) = BorderPanel.Position.West
      layout(lines) = BorderPanel.Position.Center
    }
    val bothPanel = new BorderPanel {
      layout(rotationsPanel) = BorderPanel.Position.North
      layout(stddevsPanel) = BorderPanel.Position.Center
    }
    layout(bothPanel) = BorderPanel.Position.North
  }

  reactions += {
    case Uncertainty.DefaultStdDevs3DChanged => updateDefaultStddev()
  }

  def applyChanges(updatePreviousStdDev: Boolean): Unit = {
    if (stddev.isDefined && rotationMatrix.isDefined) {
      target.asInstanceOf[Option[HasUncertainty[_3D]]] foreach { t =>
        t.uncertainty = Uncertainty(rotationMatrix.get, stddev.get)
      }
      if (updatePreviousStdDev) {
        previousStddev = stddev
        updatePreviousStddev()
      }
    }
  }

  def cleanup(): Unit = {
    target.foreach(r => deafTo(r))
    deafTo(Uncertainty)
    target = None
    stddev = None
    previousStddev = None
    originalRotationMatrix = None
    previousRotationMatrix = None
    rotationMatrix = None
    updateStddev()
    updatePreviousStddev()
    updateDefaultStddev()
    updateRotationLabels()
  }

  override def setObject(objOption: Option[AnyRef]) = {
    cleanup()
    rotationValue.peer.setMinimumSize(rotationValue.peer.getSize)
    objOption match {
      case Some(r: HasUncertainty[_3D]) =>
        target = Some(r)
        listenTo(r)
        listenTo(Uncertainty)
        updateAll()
        true
      case _ => false
    }
  }

  def updateAll() = {
    target match {
      case Some(d: HasUncertainty[_3D]) =>
        val u = d.uncertainty
        // I have *no* clue why a direct assignment leads to a weird "type mismatch" error here. Ah, whatever.
        previousStddev = Some(Vector(u.stdDevs.toArray))
        stddev = previousStddev
        originalRotationMatrix = Some(SquareMatrix(u.rotationMatrix.data))
        previousRotationMatrix = originalRotationMatrix
        rotationMatrix = previousRotationMatrix
        rotationSlider.value = 0
      case _ => /* do nothing */
    }
    updateStddev()
    updatePreviousStddev()
    updateDefaultStddev()
    updateRotationLabels()
  }

  def applyRotation() = {
    if (previousRotationMatrix.isDefined && rotationMatrix.isDefined) Try {
      val angle = Math.toRadians(rotationValue.text.toFloat)
      val axisIndex = if (rotationThird.selected) 2 else if (rotationSecond.selected) 1 else 0
      val axis = Uncertainty.Util.matrixToAxes(previousRotationMatrix.get).apply(axisIndex)

      val rm = Uncertainty.Util.rotationMatrixFor(axis, angle)
      rotationMatrix = Some(rm * previousRotationMatrix.get)
      applyChanges(updatePreviousStdDev = false)
      updateRotationLabels()
    }
  }

  def updateRotationLabels(): Unit = {
    val cells = rotationLabel.flatten
    rotationMatrix match {
      case Some(m) =>
        val data = m.t.data
        require(data.length == cells.length)
        data.indices.foreach { i =>
          cells(i).text = f"${data(i)}%.02f"
        }
      case None => cells.foreach(_.text = "X")
    }
  }

  def updateStddev() = {
    stddev match {
      case Some(v) =>
        Seq(x, y, z) zip v.toArray foreach { t =>
          t._1.enabled = true
          t._1.text = t._2.toString
        }
        toDefault.enabled = true
        set.enabled = true
      case None =>
        Seq(x, y, z) foreach { t =>
          t.enabled = false
          t.text = ""
        }
        toDefault.enabled = false
        set.enabled = false
    }
  }

  def updatePreviousStddev() = {
    Seq(reset, previousLabel) foreach (_.enabled = previousStddev.isDefined)
    previousStddev match {
      case Some(v) =>
        Seq(px, py, pz) zip v.toArray foreach { t =>
          t._1.enabled = true
          t._1.text = t._2.toString
        }
      case None =>
        Seq(px, py, pz) foreach { t =>
          t.enabled = false
          t.text = Unset
        }
    }
  }

  def updateDefaultStddev() = {
    defaultStddev = Some(Uncertainty.defaultStdDevs3D)

    Seq(dx, dy, dz).foreach(_.enabled = true)
    fromDefault.enabled = set.enabled
    val s = defaultStddev.get.toArray.map(_.toString)
    Seq(dx, dy, dz) zip s foreach (t => t._1.text = t._2)
  }

  def onRotationInput(valid: Boolean): Unit = {
    if (valid) {
      applyRotation()
    }
  }

  def onStddevInput(valid: Boolean): Unit = {
    set.enabled = valid
    toDefault.enabled = valid
    if (valid) {
      val newStdDev = Try {
        Vector(x.text.toFloat, y.text.toFloat, z.text.toFloat)
      }
      newStdDev.toOption.foreach(s => stddev = Some(s))
    }
  }

  def validateTextInput(t: TextField, function: Boolean => Unit): Boolean = {
    val f = Try(t.text.toFloat).toOption
    f match {
      case Some(v) =>
        // value is ok, restore previous border if needed
        t.border match {
          case l: ErrorBorder => t.border = l.previousBorder
          case _ => /* do nothing */
        }
      case None =>
        // value is not ok, set error border if needed
        t.border match {
          case e: ErrorBorder => /* do nothing */
          case b @ _ => t.border = new ErrorBorder(b)
        }
    }
    function(f.isDefined)
    f.isDefined
  }

  private class ErrorBorder(val previousBorder: Border) extends LineBorder(Color.RED, 2, true)

}
