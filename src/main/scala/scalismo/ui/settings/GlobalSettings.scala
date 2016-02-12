package scalismo.ui.settings

object GlobalSettingsFile extends SettingsFile(SettingsDirectory.get().get, "global.ini")
object GlobalSettings extends PersistentSettings(GlobalSettingsFile)
