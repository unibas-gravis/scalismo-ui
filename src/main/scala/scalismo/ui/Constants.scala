package scalismo.ui

import java.awt.Color

object Constants {

  object Visualization {
    /* This is the color value that is used as the basic background color when presenting color selections.
     * It is not totally in line with the actual background color used (black) - however
     * setting this too dark, or too bright, will result in a perceived mismatch between the selected value
     * and what is actually seen on screen in the render window.
     */
    var PerceivedBackgroundColor = Color.GRAY
  }

}
