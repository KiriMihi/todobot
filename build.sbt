import sbt._
import Settings._

lazy val domain = project
  .settings(commonSettings)

lazy val storage = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= storageDependencies)
  .dependsOn(domain)

lazy val service = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= serviceDependencies)
  .settings(higherKinds)
  .dependsOn(storage)

lazy val backend = project
  .settings(commonSettings)
  .dependsOn(service)




lazy val `kirimihi-todo` = Project("BilToDoBot", file("."))
  .settings(commonSettings)
  .settings(organization := "kirimihi.io")
  .settings(moduleName := "kirimihi-todo")
  .settings(name := "kirimihi-todo")
  .aggregate(
    domain,
    storage,
    service,
    backend
  )