package scalismo.ui.rendering.internal

import javax.media.opengl.GLCapabilitiesImmutable
import javax.media.opengl.awt.GLJPanel

import scalismo.ui.view.ViewportPanel

class GLJPanelWithViewport(val viewport: ViewportPanel, capabilities: GLCapabilitiesImmutable) extends GLJPanel(capabilities) {

}
