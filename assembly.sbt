import sbtassembly.Plugin.AssemblyKeys._

// put this at the top of the file

assemblySettings

jarName in assembly := "scalismo-ui.jar"

mainClass in assembly := Some("scalismo.ui.app.ScalismoViewer")

fork in run := true

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", s) if s.endsWith(".SF") || s.endsWith(".DSA") || s.endsWith(".RSA") || s.endsWith(".txt") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
}


