import Settings._

// Global Settings
ThisBuild / scalaVersion    := "2.13.8"
ThisBuild / organization    := "ffakenz"
ThisBuild / versionScheme   := Some("early-semver")
ThisBuild / conflictManager := ConflictManager.latestRevision
ThisBuild / javacOptions ++= Seq("-source", "17", "-target", "17")
ThisBuild / scalacOptions ++= Seq("-Ymacro-annotations", "-target:jvm-17")

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
  .settings(commonSettings, scalafixSettings)
  .enablePlugins(ScalafixPlugin, BuildInfoPlugin)

lazy val server = project
  .settings(
    name := "server"
  )
  .settings(commonSettings, scalafixSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Zio.all,
      Dependencies.JsonSchemaValidator.all
    ).flatten
  )
  .enablePlugins(ScalafixPlugin, BuildInfoPlugin)
  .dependsOn(api)
