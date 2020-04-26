import Dependencies._
import sbt.Keys.{scalacOptions, _}
import sbt._
import sbt.util.Level
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile

object Settings {
  val commonSettings = {
    Seq(
      scalaVersion := "2.13.1",
      scalacOptions := Seq(
        "-Ymacro-annotations",
        "-deprecation",
        "-encoding", "utf-8",
        "-explaintypes",
        "-feature",
        "-unchecked",
        "-language:postfixOps",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-Xcheckinit",
        "-Xfatal-warnings"
      ),
      javacOptions ++= Seq("-g", "-source", "1.8", "-target", "1.8", "-encoding", "UTF-8"),
      logLevel := Level.Info,
      version := (version in ThisBuild).value,
      scalafmtOnCompile := true
    )
  }

  val storageDependencies = List(zio, zioCats, zioMacros) ++ doobie
  val serviceDependencies = List(zioCats, zio, zioMacros, zioTest, zioTestSbt, fs2Core, canoe) ++ circe

  val backendDependencies = List(flyway, pureconfig, h2)
  val higherKinds = addCompilerPlugin("org.typelevel" %% "kind-projector" % Version.kindProjector)
}