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
      scalafmtOnCompile := true,
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    )
  }

  val storageDependencies = List(zio, zioCats) ++ doobie
  val serviceDependencies = List(zioCats, zio, zioMacros, zioTest, zioTestSbt, fs2Core, canoe, slf4j) ++ circe

  val backendDependencies = List(flyway, pureconfig, h2)
  val higherKinds = addCompilerPlugin("org.typelevel" %% "kind-projector" % Version.kindProjector)
}