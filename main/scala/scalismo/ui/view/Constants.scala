package scalismo.ui.view

import java.awt.Color

object Constants {

  /**
   * This is the color value that is used as the basic background color when presenting color selections.
   * It is not totally in line with the actual background color used in the renderer (black) - however
   * setting this too dark, or too bright, will result in a perceived mismatch between the selected value
   * and what is actually seen on screen in the render window.
   *
   * The only place where this is currently used is in the [[scalismo.ui.view.properties.ColorPropertyPanel]] class.
   *
   * This is a var so that it can be modified by the user if needed.
   */
  var PerceivedBackgroundColor = Color.GRAY

}
