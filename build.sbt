import com.typesafe.sbt.{GitBranchPrompt, GitVersioning}
import sbt.Keys.{unmanagedSourceDirectories, _}
import sbt.{CrossVersion, Developer, Resolver, ScmInfo, _}
import com.typesafe.sbt.SbtGit.{git, useJGit}

ThisBuild / version := "0.92-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "scalismo.ui",
    organization := "ch.unibas.cs.gravis",
    scalaVersion := "3.2.2",
    homepage := Some(url("https://scalismo.org")),
    licenses := Seq("GPLv3" -> url("http://www.gnu.org/licenses/gpl-3.0.html")),
    scmInfo := Some(
      ScmInfo(url("https://github.com/unibas-gravis/scalismo-ui"), "git@github.com:unibas-gravis/scalismo-ui.git")
    ),
    developers := List(
      Developer("marcelluethi", "marcelluethi", "marcel.luethi@unibas.ch", url("https://github.com/marcelluethi"))
    ),
    publishMavenStyle := true,
    publishTo := Some(
      if (isSnapshot.value)
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    ),
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots"),
      "twitter" at "https://maven.twttr.com/"
    ),
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case _ => Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-target:jvm-1.8")
    }),
    javacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case _ => Seq("-source", "1.8", "-target", "1.8")
    }),
    libraryDependencies ++= Seq(
      "ch.unibas.cs.gravis" %% "scalismo" % "0.92-RC1",
      "org.scalatest" %% "scalatest" % "3.2.10" % "test",
      "de.sciss" %% "swingplus" % "0.5.0",
      "com.github.jiconfont" % "jiconfont-swing" % "1.0.1",
      "com.github.jiconfont" % "jiconfont-font_awesome" % "4.5.0.3",
      "com.github.jiconfont" % "jiconfont-elusive" % "2.0.2",
      "com.github.jiconfont" % "jiconfont-entypo" % "2.0.2"
    )
  )
  .enablePlugins(GitBranchPrompt)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      BuildInfoKey.action("buildTime") {
        new java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", java.util.Locale.US).format(new java.util.Date)
      },
      BuildInfoKey.action("buildTimestamp") {
        System.currentTimeMillis()
      }
    ),
    buildInfoPackage := "scalismo.ui"
  )
  .settings(
    licenses += ("GPL-3.0", url("http://opensource.org/licenses/gpl-3.0"))
  )
  .enablePlugins(AssemblyPlugin)
  .settings(
    assembly / assemblyJarName := "scalismo-ui.jar",
    assembly / mainClass := Some("scalismo.ui.app.ScalismoViewer"),
    run / fork := true,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>
        (xs map { _.toLowerCase }) match {
          case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
            MergeStrategy.discard
          case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
            MergeStrategy.discard
          case "services" :: xs =>
            MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.first
        }
      case _ => MergeStrategy.first
    }
  )
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    organizationName := "University of Basel, Graphics and Vision Research Group",
    startYear := Some(2016),
    licenses += ("GPL-3.0", url("http://www.gnu.org/licenses/gpl-3.0.en.html")),
    headerLicense := Some(HeaderLicense.GPLv3("2016", "University of Basel, Graphics and Vision Research Group"))
  )
