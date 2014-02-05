package org.statismo.stk.ui.swing.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class ColorPickerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final Method setSelectedColorFromLocation;
	private final Method getColorForLocation;
	private final JComponent delegate;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ColorPickerPanel(final ColorListener listener) {
		this.setLayout(new BorderLayout());
		try {
			Class cl = Class.forName("javax.swing.colorchooser.MainSwatchPanel");
			Constructor co = cl.getDeclaredConstructors()[0];
			
			setSelectedColorFromLocation = cl.getMethod("setSelectedColorFromLocation", new Class[]{int.class, int.class});
			setSelectedColorFromLocation.setAccessible(true);
			getColorForLocation = cl.getMethod("getColorForLocation", new Class[]{int.class, int.class});
			getColorForLocation.setAccessible(true);
			co.setAccessible(true);
			delegate = (JComponent) co.newInstance(new Object[0]);
			this.add(delegate, BorderLayout.CENTER);
			
			delegate.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {
					Color color = getColorForLocation(e.getX(), e.getY());
					if (color != null) {
						setSelectedColorFromLocation(e.getX(), e.getY());
						delegate.requestFocusInWindow();
						listener.colorChanged(color);
					}
				}
				
			});
			delegate.requestFocusInWindow();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public void setSelectedColorFromLocation(int x, int y) {
		try {
			setSelectedColorFromLocation.invoke(delegate, x,y);
		} catch (InvocationTargetException i) {
			
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public Color getColorForLocation(int x, int y) {
		try {
			return (Color) getColorForLocation.invoke(delegate, x,y);
		} catch (InvocationTargetException i) {
			return null;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public interface ColorListener {
		void colorChanged(Color c);
	}
}
