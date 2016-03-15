package scalismo.ui.settings

object GlobalSettingsFile extends SettingsFile(SettingsDirectory.get().get, "global.ini")

object GlobalSettings extends PersistentSettings(GlobalSettingsFile) {

  /**
   * Setting Keys used by the UI itself.
   */
  object Keys {
    val HighDpiScale = "common.highdpi.scale"
    val WindowHeight = "common.windowHeight"
    val WindowWidth = "common.windowWidth"
    val WindowMaximized = "common.windowMaximized"
    val PerspectiveName = "common.perspective"
    val SlicingPositionShow = "common.slicingposition.show"
    val SlicingPositionShowIntersections2D = "common.slicingposition.showIntersections2D"
    val SlicingPositionSliceOpacity3D = "common.slicingposition.sliceOpacity3D"
    val LastUsedDirectories = "common.lastUsedDirectories"
    //    final val ImageWindowLevelWindow = "common.image.windowlevel.Window"
    //    final val ImageWindowLevelLevel = "common.image.windowlevel.Level"
    //    final val TwoDClickSnapThreshold = "common.2dclick.snapthreshold"
    //    final val TwoDClickHighlight = "common.2dclick.highlight"
  }

}
