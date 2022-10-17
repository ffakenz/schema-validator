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
  .settings(scalafixSettings)
  .enablePlugins(ScalafixPlugin, BuildInfoPlugin)

lazy val server = project
  .settings(
    name := "server"
  )
  .settings(commonSettings, scalafixSettings)
  .enablePlugins(ScalafixPlugin, BuildInfoPlugin)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.JsonSchemaValidator.all,
      Dependencies.Logback.all
    ).flatten
  )
  .settings(dockerSettings, Docker / daemonUser := "daemon")
  .enablePlugins(DockerPlugin, AshScriptPlugin)
  .dependsOn(api)
