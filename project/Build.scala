import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object BuildSettings {
  val buildOrganization = "org.statismo"
  val buildVersion = "develop-SNAPSHOT"
  val buildScalaVersion = "2.10.4"
  val publishURL = Resolver.file("file", new File("/export/contrib/statismo/repo/private"))

  val buildSettings = Defaults.defaultSettings ++ Seq(
    scalacOptions ++= Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature"),
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt := ShellPrompt.buildShellPrompt)

}

// Shell prompt which show the current project,
// git branch and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info(s: => String) {}
    def error(s: => String) {}
    def buffer[T](f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
    getOrElse "-" stripPrefix "## ")

  val buildShellPrompt = {
    (state: State) =>
      {
        val currProject = Project.extract(state).currentProject.id
        "%s:%s:%s> ".format(
          currProject, currBranch, BuildSettings.buildVersion)
      }
  }
}

object Resolvers {
  private val sonatypeSnapshots = "Sonatype SNAPSHOTs" at "https://oss.sonatype.org/content/repositories/snapshots/"
  private val sonatypeRelease = "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
  private val imagej = "imagej.releases" at "http://maven.imagej.net/content/repositories/releases"
  private val twitter = "twitter" at "http://maven.twttr.com/"
  private val statismoPublic = "Statismo (public)" at "http://statismo.cs.unibas.ch/repository/public"
  private val statismoPrivate = "Statismo (private)" at "https://statismo.cs.unibas.ch/repository/private"
  val stkResolvers = Seq(statismoPublic, statismoPrivate, sonatypeSnapshots, sonatypeRelease, imagej, twitter)
}

object Creds {
  val statismoPrivate = Credentials(Path.userHome / ".ivy2" / ".credentials-statismo-private")
}

object Dependencies {
  import BuildSettings._

  val stkCore = "org.statismo" %% "stkcore" % "0.4.0"
  val scalatest = "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test"
  val scalaReflect = "org.scala-lang" % "scala-reflect" % buildScalaVersion
  val scalaSwing = "org.scala-lang" % "scala-swing" % buildScalaVersion
  val scalaAsync = "org.scala-lang.modules" %% "scala-async" % "0.9.0-M4"
  val scalaInterpreterPane = "de.sciss" %% "scalainterpreterpane" % "1.6.+"
}

object STKBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  // Sub-project specific dependencies
  val commonDeps = Seq(
    stkCore,
    scalatest,
    scalaReflect,
    scalaSwing,
    scalaAsync,
    scalaInterpreterPane)

  lazy val cdap2 = Project(
    "stk-ui",
    file("."),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= commonDeps,
      resolvers ++= stkResolvers,
      credentials += Creds.statismoPrivate,
      publishTo := Some(publishURL),
      EclipseKeys.withSource := true))

}
