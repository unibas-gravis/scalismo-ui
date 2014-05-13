import AssemblyKeys._ // put this at the top of the file

assemblySettings

jarName in assembly := "statismo-ui.jar"

mainClass in assembly := Some("org.statismo.stk.ui.swing.Launcher")

fork in run := true

