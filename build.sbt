import sbt._
import Settings._

lazy val domain = (project in file("domain"))
  .settings(commonSettings)

lazy val storage = (project in file("storage"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= storageDependencies)
  .dependsOn(domain)

lazy val service = (project in file("service"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= serviceDependencies)
  .settings(higherKinds)
  .dependsOn(storage)

lazy val backend = (project in file("backend"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= backendDependencies)
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