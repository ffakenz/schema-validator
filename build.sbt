import Settings._

// Global Settings
ThisBuild / scalaVersion    := "3.2.0"
ThisBuild / organization    := "ffakenz"
ThisBuild / versionScheme   := Some("early-semver")
ThisBuild / conflictManager := ConflictManager.latestRevision
ThisBuild / javacOptions ++= Seq("-source", "17", "-target", "17")

lazy val root = (project in file("."))
  .settings(
    name := "schema-validator"
  )
  .settings(CommandAliases.aliases)
  .aggregate(api, server)

lazy val api = project
  .settings(
    name := "api"
  )
  .enablePlugins(BuildInfoPlugin)

import com.typesafe.sbt.packager.docker._

lazy val server = project
  .settings(
    name := "server"
  )
  .settings(commonSettings)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.JsonSchemaValidator.all,
      Dependencies.Logback.all
    ).flatten
  )
  .settings(dockerSettings, Docker / daemonUser := "daemon")
  .enablePlugins(DockerPlugin, AshScriptPlugin)
  .dependsOn(api)
