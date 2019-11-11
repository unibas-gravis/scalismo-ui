import com.typesafe.sbt.{GitVersioning, GitBranchPrompt}
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import sbt.Keys._
import sbt._
import sbtbuildinfo.Plugin._
import com.typesafe.sbt.SbtGit.{git, useJGit}

object BuildSettings {
  val buildOrganization = "ch.unibas.cs.gravis"


  val buildScalaVersion = "2.12.8"

  val buildSettings = Defaults.coreDefaultSettings ++ Seq(
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2,  11)) =>  Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-target:jvm-1.6")
      case _ => Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature", "-target:jvm-1.8")
    }),
    javacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2,  11)) => Seq("-source", "1.6", "-target", "1.6")
      case _ => Seq("-source", "1.8", "-target", "1.8")
    }),
    organization := buildOrganization,
    scalaVersion := buildScalaVersion,
    crossScalaVersions := Seq("2.11.12", "2.12.8")
  ) ++ buildInfoSettings ++ Seq(
    sourceGenerators in Compile += buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, BuildInfoKey.action("buildTime") {
      new java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", java.util.Locale.US).format(new java.util.Date)
    }, BuildInfoKey.action("buildTimestamp") {
      System.currentTimeMillis()
    }),
    buildInfoPackage := "scalismo.ui")

}

object Resolvers {
  private val sonatypeSnapshots = "Sonatype SNAPSHOTs" at "https://oss.sonatype.org/content/repositories/snapshots/"
  private val sonatypeRelease = "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
  private val twitter = "twitter" at "http://maven.twttr.com/"
  val stkResolvers = Seq( Resolver.jcenterRepo, sonatypeSnapshots, sonatypeRelease, twitter)
}

object Creds {
  val scalismoPrivate = Credentials(Path.userHome / ".ivy2" / ".credentials-statismo-private")
}

object Dependencies {

  val scalismo = "ch.unibas.cs.gravis" %% "scalismo" % "develop-e89687b4a5a8a1e4ca92011e365f60e8718d6d8c"//"0.17.2"
  val scalismoNative = "ch.unibas.cs.gravis" % "scalismo-native-all" % "4.0.0"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  val swingPlus = "de.sciss" %% "swingplus" % "0.2.2"
  val jIconFont = "com.github.jiconfont" % "jiconfont-swing" % "1.0.1"
  val jIconFontAwesome = "com.github.jiconfont" % "jiconfont-font_awesome" % "4.5.0.3"
  val jIconFontElusive = "com.github.jiconfont" % "jiconfont-elusive" % "2.0.2"
  val jIconFontEntypo = "com.github.jiconfont" % "jiconfont-entypo" % "2.0.2"
}

object ScalismoUiBuild extends Build {

  import BuildSettings._
  import Dependencies._
  import Resolvers._

  // Sub-project specific dependencies
  val commonDeps = Seq(
    scalismo,
    scalismoNative,
    scalatest,
    swingPlus,
    jIconFont,
    jIconFontAwesome,
    jIconFontElusive,
    jIconFontEntypo
  )

  lazy val scalismo_ui = Project(
    "scalismo-ui",
    file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= commonDeps,
      resolvers ++= stkResolvers,
      credentials += Creds.scalismoPrivate,
      EclipseKeys.withSource := true,
      git.useGitDescribe := false,
      git.baseVersion := "develop",
      useJGit)).enablePlugins(GitBranchPrompt, GitVersioning)

}
