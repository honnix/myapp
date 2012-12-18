import sbt._
import Keys._
import com.github.siasia.WebPlugin.webSettings

object BuildSettings {
  val buildOrganization = "com.honnix"
  val buildVersion      = "1.0-SNAPSHOT"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt  := ShellPrompt.buildShellPrompt
  )
}

// Shell prompt which show the current project,
// git branch and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object Resolvers {
}

object Dependencies {
  val scalatra = "org.scalatra" % "scalatra" % "2.1.1"
  val scalate = "org.scalatra" % "scalatra-scalate" % "2.1.1"
  val specs2 = "org.scalatra" % "scalatra-specs2" % "2.1.1" % "test"
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime"
  val jetty = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container"
  val orbit = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
}

object MyBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  // Sub-project specific dependencies
  val servletDeps = Seq (
    scalatra,
    scalate,
    specs2,
    logback,
    jetty,
    orbit
  )

  lazy val myApp = Project (
    "myapp",
    file ("."),
    settings = buildSettings
  ) aggregate (hello, servlet)

  lazy val hello = Project (
    "hello",
    file ("hello"),
    settings = buildSettings
  )

  lazy val servlet = Project (
    "servlet",
    file ("servlet"),
    settings = buildSettings ++ Seq(
      classpathTypes ~= (_ + "orbit"),
      libraryDependencies ++= servletDeps      
    ) ++ webSettings
  )
}

// vim: set ts=4 sw=4 et:
