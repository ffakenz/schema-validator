import sbt.Keys._
import sbt._

import scalafix.sbt.ScalafixPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

object Settings extends CommonScalac {
  lazy val commonSettings = Seq(
    run / fork                := true,
    Test / testForkedParallel := true,
    libraryDependencies ++= Dependencies.Zio.all,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

  lazy val scalafixSettings = Seq(
    addCompilerPlugin(scalafixSemanticdb),
    semanticdbEnabled := true,
    scalafixOnCompile := true
  )

  lazy val dockerSettings = Seq(
    dockerBaseImage      := "azul/zulu-openjdk:17",
    Docker / version     := "latest",
    Docker / packageName := "ffakenz/schema-validator"
  )
}
