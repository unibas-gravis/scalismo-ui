package scalismo.ui.view.ScalarBarWindow

import javax.swing.border.TitledBorder
import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{HasScalarRange, NodeProperty, ScalarRange}
import scalismo.ui.view.properties.PropertyPanel
import scalismo.ui.view.ScalismoFrame

import scala.swing.BorderPanel
import scala.swing.event.ButtonClicked



object ScalarBarPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame) : PropertyPanel = {
    new ScalarBarPropertyPanel(frame)
  }
}

class ScalarBarPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "ScalarBar"

  private var targets: List[HasScalarRange] = Nil

  // todo I need to add a destructor to remove it once I am done
  // also have to link to the scheme chosen by the dropdown
  private var scalarBar = new ScalarBar(frame)

  implicit val test : ScalismoFrame = frame

  private val button = new FancyToggleButton {
    // do nothing no idea
    override def formattedValue(buttonValue: Int): String = s"$buttonValue%"
  }

  layout(new BorderPanel {
    val buttonPanel: BorderPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      layout(button) = BorderPanel.Position.Center
    }
    layout(buttonPanel) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North


  listenToOwnEvents()

  def listenToOwnEvents(): Unit = {
    listenTo(button)
  }

  def deafToOwnEvents(): Unit = {
    deafTo(button)
  }

  def updateUi(): Unit = {
    deafToOwnEvents()
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasScalarRange](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.scalarRange)
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.scalarRange))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ButtonClicked(click) =>
      deafToOwnEvents()
      // TODO where we bring out the color bar and remove it from sight
      if(click.selected) {
        println("I am on")

        val values = Array(targets.head.scalarRange.value.cappedMinimum, targets.head.scalarRange.value.cappedMaximum)

        println("value 0:",values(0))
        println("value 1:",values(1))

        val oldRange = ScalarRange(0, targets.head.scalarRange.value.absoluteMinimum,0,targets.head.scalarRange.value.absoluteMaximum)

        println(targets.head.scalarRange.value)
        val lowerColor = targets.head.scalarRange.colorMapping.mappingFunction(oldRange)(values(0).toDouble)
        val upperColor = targets.head.scalarRange.colorMapping.mappingFunction(oldRange)(values(1).toDouble)
        val colors = Array(lowerColor, upperColor)
        println("lower color:", lowerColor.toString)
        println("upper color:", upperColor.toString)

        scalarBar.init(colors, values)
        scalarBar.create()
      }
      else{
        println("I am off")
        scalarBar.delete()
        // I have to do this since I have a hacky approach to closing the scalar bar when the main window closes
        scalarBar = new ScalarBar(frame)
      }
      listenToOwnEvents()
  }
}
