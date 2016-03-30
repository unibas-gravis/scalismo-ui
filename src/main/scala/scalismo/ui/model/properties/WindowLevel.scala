package scalismo.ui.model.properties

/**
 * Documentation about what window/level means can be found here:
 * http://blogs.mathworks.com/steve/2006/02/17/all-about-pixel-colors-window-level/
 * In one sentence: "Making the window wider or narrower decreases or increases the display contrast;
 * moving the level left or right changes the display brightness."
 */
case class WindowLevel(window: Float, level: Float)
