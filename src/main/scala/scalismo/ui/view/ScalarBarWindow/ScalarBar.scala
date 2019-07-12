package scalismo.ui.view.ScalarBarWindow

import java.awt
import java.awt.event.{ MouseAdapter, WindowAdapter, WindowEvent}
import java.awt.font.FontRenderContext
import java.awt.{Color, Graphics, Graphics2D, LinearGradientPaint, Rectangle}

import javax.swing.{JFrame, JPanel}
import scalismo.ui.view.ScalismoFrame

import scala.swing._

class ScalarBar(val mainFrame: ScalismoFrame){
  implicit var f: JFrame = new JFrame()

  // this is to force the window to close when the main window does
  f.addWindowListener(new WindowAdapter {
    override def windowActivated(e: WindowEvent): Unit = {
      print("Hello: ",mainFrame.visible)
      if(!mainFrame.visible){
        f.dispose()
      }
    }
  })
  var colorGradient = new SimpleGradient()

  def init(colors : Array[Color], values : Array[Float]): Unit ={
    assert(colors.length == values.length)
    this.colorGradient.init(colors, values)
  }

  // need to pass that information in
  def create(): Unit ={
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.setUndecorated(true)
    f.setPreferredSize(new Dimension(100,120))
    f.setBackground(new Color(0f, 0f, 0f, 0f))

    // initialize the color gradient with the correct colors and values
    f.add(colorGradient)

    // we now have to add mouse listener
    val frameDragListener = new FrameDragListener()
    f.addMouseListener(frameDragListener)
    f.addMouseMotionListener(frameDragListener)

    f.pack()
    f.setLocationRelativeTo(null)
    f.setVisible(true)
    f.setAlwaysOnTop(true)


  }

  def delete(): Unit ={
    f.setVisible(false)
    f.dispose()
  }
}

class SimpleGradient (implicit frame : JFrame) extends JPanel{
  // this is just a template holder for these variables
  // the order is from RED -> BLUE is top -> bottom
  var colors = Array(Color.RED, Color.BLUE)
  // the order of values is left -> right is bottom -> top
  var values: Array[Float] = Array(0,50,100)

  val W = 50
  val H = 100
  this.setPreferredSize(new Dimension(W, H))

  def init(colors : Array[Color], values : Array[Float]): Unit ={
    this.colors = colors
    this.values = values
  }

  override def paintComponent(g : Graphics): Unit ={

    val offset_X = W/4-W/16
    val offset_y = 5

    val g2 = g.asInstanceOf[Graphics2D]
    val start = new Point(offset_X,offset_y)
    val end = new Point(10,100)
    val dist = Array(0.0f, 1.0f)
    val gp = new LinearGradientPaint(start, end, dist, colors)

    g2.setPaint(gp)
    //val offset_X = W/4-W/16
    //val offset_y = 5
    g2.fillRect(offset_X,offset_y,10,100)

    // set dashed lines to the gradient maybe as indication of what we want
    g.drawLine(0,offset_y,W/2, offset_y)
    g.drawLine(0, H/2 + offset_y, W/2, H/2 + offset_y)
    g.drawLine(0, H-1 + offset_y, W/2, H-1 + offset_y)

    // now we just need to add the numbers corresponding to the range

    val bounds = getStringBounds(g2, "test", 0, 0)
    val sep = 5

    var middle = (values(1) - values(0))/2
    if(middle == 0){
      middle = values(0)
    }
    g.drawString(values(1).toString, W/2+sep,0+bounds.height/2 + offset_y)
    g.drawString(middle.toString, W/2+sep, H/2+bounds.height/2 + offset_y)
    g.drawString(values(0).toString, W/2+sep, H+bounds.height/2 + offset_y)

  }
  def getStringBounds (g2: Graphics2D, str: String, x: Float, y: Float): Rectangle = {
    val frc: FontRenderContext = g2.getFontRenderContext
    val gv = g2.getFont.createGlyphVector (frc, str)
    gv.getPixelBounds (null, x, y)
  }
}


class FrameDragListener (implicit frame : JFrame) extends MouseAdapter{
  var mouseDownCompCoords : Point = _

  override def mouseReleased(e: awt.event.MouseEvent): Unit = {
    this.mouseDownCompCoords = _ : Point
  }

  override def mouseClicked(e: awt.event.MouseEvent): Unit = {
    println("x:"+e.getPoint.x + ", y:"+e.getPoint.y)
  }

  override def mousePressed(e: awt.event.MouseEvent): Unit = {
    this.mouseDownCompCoords = e.getPoint
  }

  override def mouseDragged(e: awt.event.MouseEvent): Unit = {
    val currCoords = e.getLocationOnScreen
    frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y)
  }
}


