package org.statismo.stk.ui.swing

import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.Nameable
import scala.swing.Slider
import scala.swing.BorderPanel
import scala.swing.event.ValueChanged
import scala.swing.Component
import javax.swing.JColorChooser
import scala.swing.Swing
import java.awt.Color
import scala.swing.event.Event
import javax.swing.colorchooser.DefaultSwatchChooserPanel
import org.statismo.stk.ui.swing.util.ColorPickerPanel
import javax.swing.border.TitledBorder
import scala.swing.Label
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import java.awt.Graphics
import scala.swing.Button
import scala.swing.TextField
import scala.swing.event.ButtonClicked
import scala.swing.GridPanel

class NameablePanel extends BorderPanel with SceneObjectPropertyPanel {
  val description = "Name"
  private var target: Option[Nameable] = None

  lazy val okButton = new Button("Apply")
  lazy val cancelButton = new Button("Cancel")
  lazy val nameField = new TextField
  
  listenTo(okButton, cancelButton)
  
  {
     val buttons = new GridPanel(1, 2) {
       contents ++= Seq(cancelButton, okButton)
     }
    val bothPanel = new BorderPanel {
      layout(nameField) = BorderPanel.Position.Center
      layout(buttons) = BorderPanel.Position.South
    }
    layout(bothPanel) = BorderPanel.Position.North
  }
  
  def cleanup() = {
    if (target.isDefined) {
      deafTo(target.get)
      target = None
    }
  }
  
  reactions += {
    case Nameable.NameChanged(s) => updateUi
    case ButtonClicked(b) => {
      if (target.isDefined) {
	      if (b == cancelButton) {
	        nameField.text = target.get.name
	      } else if (b == okButton) {
	        target.get.name = nameField.text
	      }
      }
    }
  }

  def setObject(obj: Option[SceneTreeObject]): Boolean = {
	cleanup()
    if (obj.isDefined && obj.get.isInstanceOf[Nameable] && obj.get.asInstanceOf[Nameable].isNameModifiable) {
      target = Some(obj.get.asInstanceOf[Nameable])
      updateUi()
      listenTo(target.get)
      true
    } else {
      false
    }
  }

  def updateUi() = {
    if (target.isDefined) {
      nameField.text = target.get.name
    }
  }
}