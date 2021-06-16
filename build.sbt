import com.typesafe.sbt.{GitBranchPrompt, GitVersioning}
import sbt.Keys._
import sbt.{CrossVersion, Developer, Resolver, ScmInfo, _}

import com.typesafe.sbt.SbtGit.{git, useJGit}

lazy val root = (project in file("."))
  .settings(
    name := "scalismo.ui",
    organization := "ch.unibas.cs.gravis",
    scalaVersion := "2.12.6",
    crossScalaVersions := Seq("2.11.12", "2.12.6"),
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
      "twitter" at "http://maven.twttr.com/"
    ),
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) =>
        Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-target:jvm-1.6")
      case _ => Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-target:jvm-1.8")
    }),
   javacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2,  11)) => Seq("-source", "1.6", "-target", "1.6")
      case _ => Seq("-source", "1.8", "-target", "1.8")
    }),
    libraryDependencies ++= Seq(
      "ch.unibas.cs.gravis" %% "scalismo" % "0.18.0",
      "ch.unibas.cs.gravis" % "scalismo-native-all" % "4.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "de.sciss" %% "swingplus" % "0.2.2",
      "com.github.jiconfont" % "jiconfont-swing" % "1.0.1",
      "com.github.jiconfont" % "jiconfont-font_awesome" % "4.5.0.3",
      "com.github.jiconfont" % "jiconfont-elusive" % "2.0.2",
      "com.github.jiconfont" % "jiconfont-entypo" % "2.0.2"
    )
  )
  .enablePlugins(GitVersioning)
  .settings(
    git.baseVersion := "develop",
    git.useGitDescribe := false,
    useJGit
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
    assemblyJarName in assembly := "scalismo-ui.jar",
    mainClass in assembly := Some("scalismo.ui.app.ScalismoViewer"),
    fork in run := true,
    assemblyMergeStrategy in assembly ~= { _ =>
      {
        case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
        case PathList("META-INF", s)
            if s.endsWith(".SF") || s.endsWith(".DSA") || s.endsWith(".RSA") || s.endsWith(".txt") =>
          MergeStrategy.discard
        case _ => MergeStrategy.first
      }
    }
  )
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    organizationName := "University of Basel, Graphics and Vision Research Group",
    startYear := Some(2016),
    licenses += ("GPL-3.0", url("http://www.gnu.org/licenses/gpl-3.0.en.html")),
    headerLicense := Some(HeaderLicense.GPLv3("2016", "University of Basel, Graphics and Vision Research Group"))
  )

libraryDependencies += "com.github.ghik" % "silencer-lib" % "0.4"
addCompilerPlugin("com.github.ghik" % "silencer-plugin" % "0.4")
