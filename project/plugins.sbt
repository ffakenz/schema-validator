// Versioning
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

// Packing
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")

// Linting & Styling
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.1")

// WatchMode
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addDependencyTreePlugin
