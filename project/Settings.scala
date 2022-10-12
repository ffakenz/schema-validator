import sbt.Keys._
import sbt._

import scalafix.sbt.ScalafixPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.docker._

object Settings extends CommonScalac {
  lazy val commonSettings = Seq(
    run / fork                := true,
    Test / testForkedParallel := true,
    libraryDependencies ++= Dependencies.Zio.all,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

  lazy val scalafixSettings: Seq[Setting[_]] = Seq(
    addCompilerPlugin(scalafixSemanticdb),
    semanticdbEnabled := true,
    scalafixOnCompile := true
  )
}
