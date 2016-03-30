package scalismo.ui.view.util

import java.awt.Color

/**
 * The values in here are constants, in the sense that they're not supposed to change while an
 * application is running. However, they are still defined as vars, to allow developers to
 * easily override them if needed. If that is done, it should be done as early as possible
 * (e.g. in the main method), before an actual instance of the UI is created.
 */
object Constants {

  /**
   * This is the color value that is used as the basic background color when presenting color selections.
   * It is not totally in line with the actual background color used in the renderer (black) - however
   * setting this too dark, or too bright, will result in a perceived mismatch between the selected value
   * and what is actually seen on screen in the render window.
   *
   * The only place where this is currently used is in the [[scalismo.ui.view.properties.ColorPropertyPanel]] class.
   *
   */
  var PerceivedBackgroundColor: Color = Color.GRAY

  /**
   * This is the default size of icons, *before* any scaling is applied.
   */
  var StandardUnscaledIconSize: Int = 16

  /**
   * The default font size.
   * This seems to be baked in deeply in all of the Swing internals, so
   * DO NOT CHANGE THIS VALUE unless you REALLY know what you are doing.
   */
  var DefaultFontSize: Int = 12

}
