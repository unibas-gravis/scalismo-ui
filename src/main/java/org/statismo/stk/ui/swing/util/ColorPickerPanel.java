// TODO: Make sure that we abide to the license.
// This code was taken from the class "com.bric.swing.ColorPickerPanel", and minimally adjusted to get rid of unneeded stuff, and dependencies to another class
/*
 * @(#)ColorPickerPanel.java
 *
 * $Date: 2012-07-03 01:10:05 -0500 (Tue, 03 Jul 2012) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * http://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 * 
 * 
 * === LICENSE ===
<html>
Source code, binaries and/or any other resources in the package labeled "com.bric" are copyright (c) 2014 by Jeremy Wood.  They are available under the Modified BSD license (see below).
<P>Any resources not in the "com.bric" package may be subject to additional license terms and restrictions.
<P>If you have any questions about this jar, the relevant licenses, the source code, etc., please contact <A HREF="mailto:mickleness+java@gmail.com">mickleness+java@gmail.com</A>.
<P>This jar is part of the "javagraphics" project, discussed <A HREF="http://javagraphics.java.net/">here</A>
<h3>Modified BSD License</H3>
<P>Copyright (c) 2014, Jeremy Wood.
<BR>All rights reserved.
<P>Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

<BR> * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
<BR> * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
<BR> * The name of the contributors may not be used to endorse or promote products derived from this software without specific prior written permission.
<P>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
</html>
 * 
 */
package org.statismo.stk.ui.swing.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

/** This is the large graphic element in the <code>ColorPicker</code>
 * that depicts a wide range of colors.
 * <P>This panel can operate in 6 different modes.  In each mode a different
 * property is held constant: hue, saturation, brightness, red, green, or blue.
 * (Each property is identified with a constant in the <code>ColorPicker</code> class,
 * such as: <code>ColorPicker.HUE</code> or <code>ColorPicker.GREEN</code>.)
 * <P>In saturation and brightness mode, a wheel is used.  Although it doesn't
 * use as many pixels as a square does: it is a very aesthetic model since the hue can
 * wrap around in a complete circle.  (Also, on top of looks, this is how most
 * people learn to think the color spectrum, so it has that advantage, too).
 * In all other modes a square is used.
 * <P>The user can click in this panel to select a new color.  The selected color is
 * highlighted with a circle drawn around it.  Also once this
 * component has the keyboard focus, the user can use the arrow keys to
 * traverse the available colors.
 * <P>Note this component is public and exists independently of the
 * <code>ColorPicker</code> class.  The only way this class is dependent
 * on the <code>ColorPicker</code> class is when the constants for the modes
 * are used.
 * <P>The graphic in this panel will be based on either the width or
 * the height of this component: depending on which is smaller.
 *
 */
public class ColorPickerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	/** The maximum size the graphic will be.  No matter
	 *  how big the panel becomes, the graphic will not exceed
	 *  this length.
	 *  <P>(This is enforced because only 1 BufferedImage is used
	 *  to render the graphic.  This image is created once at a fixed
	 *  size and is never replaced.)
	 */
	public static final int MAX_SIZE = 325;
	
	/** This controls how the colors are displayed. */
//	private int mode = ColorPicker.BRI;
	
	/** The point used to indicate the selected color. */
	private Point point = new Point(0,0);
	
	private Vector<ChangeListener> changeListeners = new Vector<ChangeListener>();
	
	/* Floats from [0,1].  They must be kept distinct, because
	 * when you convert them to RGB coordinates HSB(0,0,0) and HSB (.5,0,0)
	 * and then convert them back to HSB coordinates, the hue always shifts back to zero.
	 */
	float hue = -1, sat = -1, bri = -1;
	int red = -1, green = -1, blue = -1;
	
	MouseInputListener mouseListener = new MouseInputAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			requestFocus();
			Point p = e.getPoint();
			float[] hsb = getHSB(p);
			setHSB(hsb[0], hsb[1], hsb[2]);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mousePressed(e);
		}
	};
	
	KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int dx = 0;
			int dy = 0;
			if(e.getKeyCode()==KeyEvent.VK_LEFT) {
				dx = -1;
			} else if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
				dx = 1;
			} else if(e.getKeyCode()==KeyEvent.VK_UP) {
				dy = -1;
			} else if(e.getKeyCode()==KeyEvent.VK_DOWN) {
				dy = 1;
			}
			int multiplier = 1;
			if(e.isShiftDown() && e.isAltDown()) {
				multiplier = 10;
			} else if(e.isShiftDown() || e.isAltDown()) {
				multiplier = 5;
			}
			if(dx!=0 || dy!=0) {
				int size = Math.min(MAX_SIZE, Math.min(getWidth()-imagePadding.left-imagePadding.right,getHeight()-imagePadding.top-imagePadding.bottom));
				
				int offsetX = getWidth()/2-size/2;
				int offsetY = getHeight()/2-size/2;
				mouseListener.mousePressed(new MouseEvent(ColorPickerPanel.this,
						MouseEvent.MOUSE_PRESSED,
						System.currentTimeMillis(), 0,
						point.x+multiplier*dx+offsetX,
						point.y+multiplier*dy+offsetY,
						1, false
						));
			}
		}
	};
	
	FocusListener focusListener = new FocusListener() {
		public void focusGained(FocusEvent e) {
			repaint();
		}
		public void focusLost(FocusEvent e) {
			repaint();
		}
	};
	
	ComponentListener componentListener = new ComponentAdapter() {

		@Override
		public void componentResized(ComponentEvent e) {
			regeneratePoint();
			regenerateImage();
		}
		
	};
	
	BufferedImage image = new BufferedImage(MAX_SIZE, MAX_SIZE, BufferedImage.TYPE_INT_ARGB);
	
	/** Creates a new <code>ColorPickerPanel</code> */
	public ColorPickerPanel() {
		setMaximumSize(new Dimension(MAX_SIZE+imagePadding.left+imagePadding.right, 
				MAX_SIZE+imagePadding.top+imagePadding.bottom));
		setPreferredSize(new Dimension( (int)(MAX_SIZE*.75), (int)(MAX_SIZE*.75)));
		
		setRGB(0,0,0);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		
		setFocusable(true);
		addKeyListener(keyListener);
		addFocusListener(focusListener);

		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		addComponentListener(componentListener);
	}
	
	/** This listener will be notified when the current HSB or RGB values
	 * change.
	 */
	public void addChangeListener(ChangeListener l) {
		if(changeListeners.contains(l))
			return;
		changeListeners.add(l);
	}
	
	/** Remove a <code>ChangeListener</code> so it is no longer
	 * notified when the selected color changes.
	 */
	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}
	
	protected void fireChangeListeners() {
		if(changeListeners==null)
			return;
		for(int a = 0; a<changeListeners.size(); a++) {
			ChangeListener l = changeListeners.get(a);
			try {
				l.stateChanged(new ChangeEvent(this));
			} catch(RuntimeException e) {
				e.printStackTrace();
			}
		}
	}
	
	Insets imagePadding = new Insets(6,6,6,6);
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g;
		int size = Math.min(MAX_SIZE, Math.min(getWidth()-imagePadding.left-imagePadding.right,getHeight()-imagePadding.top-imagePadding.bottom));
		
		g2.translate(getWidth()/2-size/2, getHeight()/2-size/2);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Shape shape = new Ellipse2D.Float(0,0,size,size);
		
		//paint a circular shadow
		g2.translate(2,2);
		g2.setColor(new Color(0,0,0,20));
		g2.fill(new Ellipse2D.Float(-2,-2,size+4,size+4));
		g2.setColor(new Color(0,0,0,40));
		g2.fill(new Ellipse2D.Float(-1,-1,size+2,size+2));
		g2.setColor(new Color(0,0,0,80));
		g2.fill(new Ellipse2D.Float(0,0,size,size));
		g2.translate(-2,-2);
		
		g2.drawImage(image, 0, 0, size, size, 0, 0, size, size, null);
		
		g2.setStroke(new BasicStroke(1));
		g2.setColor(new Color(0,0,0,120));
		g2.draw(shape);
		
		g2.setColor(Color.white);
		g2.setStroke(new BasicStroke(1));
		g2.draw(new Ellipse2D.Float(point.x-3,point.y-3,6,6));
		g2.setColor(Color.black);
		g2.draw(new Ellipse2D.Float(point.x-4,point.y-4,8,8));
		
		g.translate(-imagePadding.left, -imagePadding.top);
	}
	
	/** Sets the selected color of this panel.
	 * <P>If this panel is in HUE, SAT, or BRI mode, then
	 * this method converts these values to HSB coordinates
	 * and calls <code>setHSB</code>.
	 * <P>This method may regenerate the graphic if necessary.
	 * 
	 * @param r the red value of the selected color.
	 * @param g the green value of the selected color.
	 * @param b the blue value of the selected color.
	 */
	public void setRGB(int r,int g,int b) {
		if(r<0 || r>255)
			throw new IllegalArgumentException("The red value ("+r+") must be between [0,255].");
		if(g<0 || g>255)
			throw new IllegalArgumentException("The green value ("+g+") must be between [0,255].");
		if(b<0 || b>255)
			throw new IllegalArgumentException("The blue value ("+b+") must be between [0,255].");
		
		if(red!=r || green!=g || blue!=b) {
			float[] hsb = new float[3];
			Color.RGBtoHSB(r, g, b, hsb);
			setHSB(hsb[0],hsb[1],hsb[2]);
		}
	}
	
	/** @return the HSB values of the selected color.
	 * Each value is between [0,1].
	 */
	public float[] getHSB() {
		return new float[] {hue, sat, bri};
	}
	
	/** @return the RGB values of the selected color.
	 * Each value is between [0,255].
	 */
	public int[] getRGB() {
		return new int[] {red, green, blue};
	}
	
	/** Returns the color at the indicated point in HSB values.
	 * 
	 * @param p a point relative to this panel.
	 * @return the HSB values at the point provided.
	 */
	public float[] getHSB(Point p) {
		int size = Math.min(MAX_SIZE, Math.min(getWidth()-imagePadding.left-imagePadding.right,getHeight()-imagePadding.top-imagePadding.bottom));
		p.translate(-(getWidth()/2-size/2), -(getHeight()/2-size/2));
		//the two circular views:
		double radius = (size)/2.0;
		double x = p.getX()-size/2.0;
		double y = p.getY()-size/2.0;
		double r = Math.sqrt(x*x+y*y)/radius;
		double theta = Math.atan2(y,x)/(Math.PI*2.0);
		
		if(r>1) r = 1;
		
		return new float[] { 
				(float)(theta+.25f),
				(float)(r),
				bri};
	}

	/** Returns the color at the indicated point in RGB values.
	 * 
	 * @param p a point relative to this panel.
	 * @return the RGB values at the point provided.
	 */
	public int[] getRGB(Point p) {
		float[] hsb = getHSB(p);
		int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		int r = (rgb & 0xff0000) >> 16;
		int g = (rgb & 0xff00) >> 8;
		int b = (rgb & 0xff);
		return new int[] {r, g, b};
	}

	/** Sets the selected color of this panel.
	 * <P>If this panel is in RED, GREEN, or BLUE mode, then
	 * this method converts these values to RGB coordinates
	 * and calls <code>setRGB</code>.
	 * <P>This method may regenerate the graphic if necessary.
	 * 
	 * @param h the hue value of the selected color.
	 * @param s the saturation value of the selected color.
	 * @param b the brightness value of the selected color.
	 */
	public void setHSB(float h,float s,float b) {
		//hue is cyclic: it can be any value
		h = (float)(h-Math.floor(h));
		
		if(s<0 || s>1)
			throw new IllegalArgumentException("The saturation value ("+s+") must be between [0,1]");
		if(b<0 || b>1)
			throw new IllegalArgumentException("The brightness value ("+b+") must be between [0,1]");
		
		if(hue!=h || sat!=s || bri!=b) {
			float lastBri = bri;
			hue = h;
			sat = s;
			bri = b;
			
			if(lastBri!=bri) {
				regenerateImage();
			}
		}
			

		Color c = new Color(Color.HSBtoRGB(hue, sat, bri));
		red = c.getRed();
		green = c.getGreen();
		blue = c.getBlue();
		
		regeneratePoint();
		repaint();
		fireChangeListeners();
	}
	
	/** Recalculates the (x,y) point used to indicate the selected color. */
	private void regeneratePoint() {
		int size = Math.min(MAX_SIZE, Math.min(getWidth()-imagePadding.left-imagePadding.right,getHeight()-imagePadding.top-imagePadding.bottom));
		double theta = hue*2*Math.PI-Math.PI/2;
		if(theta<0) theta+=2*Math.PI;
		double r = sat*size/2;
		point = new Point((int)(r*Math.cos(theta)+.5+size/2.0),(int)(r*Math.sin(theta)+.5+size/2.0));
	}
	
	/** A row of pixel data we recycle every time we regenerate this image. */
	private int[] row = new int[MAX_SIZE];
	/** Regenerates the image. */
	private synchronized void regenerateImage() {
		int size = Math.min(MAX_SIZE, Math.min(getWidth()-imagePadding.left-imagePadding.right,getHeight()-imagePadding.top-imagePadding.bottom));
		
		float bri2 = this.bri;
		float sat2 = this.sat;
		float radius = (size)/2f;
		float hue2;
		float k = 1.2f; //the number of pixels to antialias
		for(int y = 0; y<size; y++) {
			float y2 = (y-size/2f);
			for(int x = 0; x<size; x++) {
				float x2 = (x-size/2f);
				double theta = Math.atan2(y2,x2)-3*Math.PI/2.0;
				if(theta<0) theta+=2*Math.PI;
				
				double r = Math.sqrt(x2*x2+y2*y2);
				if(r<=radius) {
					hue2 = (float)(theta/(2*Math.PI));
					sat2 = (float)(r/radius);
					row[x] = Color.HSBtoRGB(hue2, sat2, bri2);
					if(r>radius-k) {
						int alpha = (int)(255-255*(r-radius+k)/k);
						if(alpha<0) alpha = 0;
						if(alpha>255) alpha = 255;
						row[x] = row[x] & 0xffffff+(alpha << 24);
					}
				} else {
					row[x] = 0x00000000;
				}
			}
			image.getRaster().setDataElements(0, y, size, 1, row);
			}
		repaint();
	}
}
