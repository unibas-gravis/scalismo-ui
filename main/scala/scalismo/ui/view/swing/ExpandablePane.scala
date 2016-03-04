package scalismo.ui.view.swing

import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.{ Container, Dimension }
import javax.swing.{ JComponent, JSplitPane }

object ExpandablePane {
  private[ExpandablePane] val ZeroSize: Dimension = new Dimension(0, 0)

  // this is a def, rather than a val, so that a new component gets created for every instance.
  private[ExpandablePane] def zeroComponent: JComponent = new JComponent {
    override def getPreferredSize: Dimension = ZeroSize

    override def getMinimumSize: Dimension = ZeroSize
  }
}

class ExpandablePane(orientation: Int, leftOrTop: java.awt.Component) extends JSplitPane(orientation, null, null) {

  // if true, the main component is the right/bottom component.
  // don't set it at this time, it currently doesn't work correctly, especially for horizontal layouts.
  protected def reverseOrder: Boolean = false

  private def isVertical = orientation == JSplitPane.VERTICAL_SPLIT

  // indicates if the component is deemed to be functional at all. In case it isn't,
  // the drag handle will still be displayed, but simply won't have any effect.
  val operational: Boolean = getUI match {
    case synth: javax.swing.plaf.synth.SynthSplitPaneUI =>
      setUI(new SynthUI)
      true
    case _ =>
      //println(s"Warning: no class match found for ${getUI.getClass}; ExpandablePane will not work correctly.")
      false
  }

  // is the user currently dragging the handle?
  private var dragging = false

  // initialization
  if (!reverseOrder) {
    setLeftComponent(leftOrTop)
    setRightComponent(ExpandablePane.zeroComponent)
  } else {
    setLeftComponent(ExpandablePane.zeroComponent)
    setRightComponent(leftOrTop)
  }

  private def pane = this

  override def setPreferredSize(size: Dimension): Unit = {
    super.setPreferredSize(size)
    invalidate()
    getParent.revalidate()
  }

  override def getPreferredSize: Dimension = {
    // on the very first call, calculate the preferred size and store it.
    // on all subsequent calls, return the stored value.
    if (operational && !isPreferredSizeSet) {
      val auto = super.getPreferredSize
      setPreferredSize(new Dimension(auto.width, auto.height))
    }
    super.getPreferredSize
  }

  override def getSize: Dimension = {
    val real: Dimension = super.getSize

    // if the user is actively dragging, we return the maximum
    // possible size instead of the actual current size, to
    // allow for dragging beyond the current size (i.e.,
    // to allow for enlarging the panel)
    if (operational && dragging) {
      // this currently travels all the way up to a
      // root container (usually a frame).
      def containing(current: Container): Container = {
        val parent = current.getParent
        if (parent == null) current else containing(parent)
      }
      val fake = containing(this).getSize

      if (isVertical) {
        fake.width = real.width
      } else {
        fake.height = real.height
      }
      fake
    } else real
  }

  private trait BasicUIOverrides extends javax.swing.plaf.basic.BasicSplitPaneUI {

    override def createDefaultDivider = {
      val divider = super.createDefaultDivider
      divider.addMouseListener(new MouseAdapter {
        override def mousePressed(e: MouseEvent): Unit = {
          dragging = true
        }

        override def mouseReleased(e: MouseEvent): Unit = {
          dragging = false
        }
      })
      divider
    }

    override def dragDividerTo(location: Int): Unit = {
      val last = getLastDragLocation
      super.dragDividerTo(location)
      if (last >= 0) {
        val diff = location - last
        //println(s"dragDividerTo($location), last = $last, diff = $diff ")
        if (diff != 0) {
          val currentSize = pane.getPreferredSize
          val newSize = {
            if (isVertical) {
              new Dimension(currentSize.width, currentSize.height + diff)
            } else {
              new Dimension(currentSize.width + diff, currentSize.height)
            }
          }
          pane.setPreferredSize(newSize)
        }
      }
    }
  }

  private class SynthUI extends javax.swing.plaf.synth.SynthSplitPaneUI with BasicUIOverrides

}
