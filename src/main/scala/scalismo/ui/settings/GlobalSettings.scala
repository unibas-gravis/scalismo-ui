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
    val LastUsedDirectories = "common.lastUsedDirectories"
    //    final val TwoDClickSnapThreshold = "common.2dclick.snapthreshold"
    //    final val TwoDClickHighlight = "common.2dclick.highlight"
  }

}
