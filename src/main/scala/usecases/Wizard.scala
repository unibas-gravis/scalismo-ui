package usecases

import org.statismo.stk.ui.{Workspace, StatismoFrame, Scene, StatismoApp}
import org.statismo.stk.ui.swing._
import scala.swing._

/* This class is just a really simple and crude example for now. It needs to be revised to become more useful.*/
object Wizard extends SimpleSwingApplication with StatismoLookAndFeel {

  lazy val scene = new Scene
  lazy val workspace = new Workspace(scene)


  val MaxSteps = 3

  lazy val previous = new Button(new Action("<-- previous") {
    override def apply(): Unit = {
      current = new WizardPanel(current.step-1)
    }
  })

  lazy val next = new Button(new Action("next -->") {
    override def apply(): Unit = {
      current = new WizardPanel(current.step+1)
    }
  })

  var _current = new WizardPanel(1)
  def current = _current
  def current_=(n: WizardPanel): Unit = {
    _current = n
    mainPanel.layout(n) = BorderPanel.Position.Center
    mainPanel.revalidate()
  }

  lazy val mainPanel = new BorderPanel{
    val buttons = new BoxPanel(Orientation.Horizontal)
    buttons.contents += (previous, next)
    layout(buttons) = BorderPanel.Position.North
    layout(current) = BorderPanel.Position.Center
  }

  override lazy val top = new MainFrame() {
    contents = mainPanel
  }

  class WizardPanel(val step: Integer) extends WorkspacePanel(workspace) {
    previous.enabled = step > 1
    next.enabled = step < MaxSteps

    val txt = s"this is step $step"
    toolbar.add(new Action(txt) {
      override def apply(): Unit = println(txt)
    })
  }
}