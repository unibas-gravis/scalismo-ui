package scalismo.ui.view.ScalarBarWindow

import java.awt.event.ActionEvent

import javax.swing.border.TitledBorder
import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{BlueToRedColorMapping, GreenToBlack, HasScalarRange, NodeProperty, WhiteToBlackMapping}
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.properties.PropertyPanel
import java.awt.Color

import scala.swing.BorderPanel

object DropDownPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame) : PropertyPanel = {
    new DropDownPropertyPanel(frame)
  }
}

class DropDownPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {

  private var targets: List[HasScalarRange] = Nil
  private var min: Float = 0
  private var max: Float = 100
  private var step: Float = 1

  override def description: String = "ColorScheme"

  // TODO change here if we want to have different schemes or more schemes
  val items = Array("Blue-Red", "White-Black", "Green-Black")

  val dropDown: FancyDropDown = new FancyDropDown(items){
    override def actionPerformed(e: ActionEvent): Unit = {
      updateColorMapping()
    }
  }

  listenToOwnEvents()

  def listenToOwnEvents(): Unit = {
    listenTo(dropDown)
  }

  def deafToOwnEvents(): Unit = {
    deafTo(dropDown)
  }

  def fromSliderValue(v: Int): Float = {
    v * step + min
  }

  def updateColorMapping() {
    //println("I am updating targets with: ", dropDown.item)

    if(dropDown.item == items(0)){
      targets.foreach(t => {
        t.scalarRange.colorMapping = BlueToRedColorMapping
      })
    }
    else if(dropDown.item == items(1)){
      targets.foreach(t => {
        t.scalarRange.colorMapping = WhiteToBlackMapping
      })
    }
    else if(dropDown.item == items(2)){
      targets.foreach(t => {
        t.scalarRange.colorMapping = GreenToBlack
      })
    }

    // this is to propagate the change when I change the toggle
    // I copied this from the slider and I feel like I could make this a lot more simple
    val (fMin, fMax) = (fromSliderValue(targets.head.scalarRange.value.cappedMinimum.toInt), fromSliderValue(targets.head.scalarRange.value.cappedMaximum.toInt))
    targets.foreach(t => t.scalarRange.value = t.scalarRange.value.copy(cappedMinimum = fMin, cappedMaximum = fMax))
  }


  def updateUi(): Unit ={
    deafToOwnEvents()
    targets.foreach{ t =>
      min = t.scalarRange.value.absoluteMinimum
      max = t.scalarRange.value.absoluteMaximum
      step = (max - min) / 100.0f
    }
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

  layout(new BorderPanel {
    val dropDownPanel: BorderPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      layout(dropDown) = BorderPanel.Position.Center
    }
    layout(dropDownPanel) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
  }
}

