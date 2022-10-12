import sbt._

object Dependencies {
  object Versions {
    val zio                 = "2.0.2"
    val zioActors           = "0.1.0"
    val zioTest             = "2.0.0"
    val zioConfig           = "2.0.9"
    val zioLogging          = "2.1.2"
    val zioJson             = "0.3.0"
    val zioHttp             = "2.0.0-RC11"
    val jsonSchemaValidator = "2.2.14"
  }

  object Zio {
    val zio             = "dev.zio" %% "zio"                 % Versions.zio
    val zioTest         = "dev.zio" %% "zio-test"            % Versions.zioTest % Test
    val zioTestSbt      = "dev.zio" %% "zio-test-sbt"        % Versions.zioTest % Test
    val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia"   % Versions.zioTest % Test
    val zioConfig       = "dev.zio" %% "zio-config"          % Versions.zioConfig
    val zioTypesafe     = "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
    val zioLogging      = "dev.zio" %% "zio-logging"         % Versions.zioLogging
    val zioLoggingSlf4j = "dev.zio" %% "zio-logging-slf4j"   % Versions.zioLogging
    val zioJson         = "dev.zio" %% "zio-json"            % Versions.zioJson
    val zioHttp         = "io.d11"  %% "zhttp"               % Versions.zioHttp

    val all = Seq(
      zio,
      zioTest,
      zioTestSbt,
      zioTestMagnolia,
      zioConfig,
      zioTypesafe,
      zioLogging,
      zioLoggingSlf4j,
      zioJson,
      zioHttp
    )
  }

  object JsonSchemaValidator {
    val jsonSchemaValidator = "com.github.java-json-tools" % "json-schema-validator" % Versions.jsonSchemaValidator

    val all = Seq(
      jsonSchemaValidator
    )
  }
}
