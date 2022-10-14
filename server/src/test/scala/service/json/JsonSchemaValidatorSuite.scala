package service.json

import zio.{ ZIO, Ref }
import zio.test.{ test, _ }
import model.json._
import zio.ZLayer
import zio.ZEnvironment
import model.domain._
import zio.{ Scope, UIO }
import service.json.JsonSchemaValidator
import infra.json.Layers
import com.github.fge.jackson.JacksonUtils
import java.io.IOException
import scala.io.{ Source, BufferedSource }

object JsonSchemaValidatorSuite {

  def jsonSuite =
    suite("JSON Suite")(
      testSuccess,
      testFailure
    ).provide(
      Layers.jsonValidator,
      Layers.jsonValidatorClient,
      JsonSchemaValidator.layer,
      Scope.default
    )

  def testSuccess =
    test("validate success") {
      val json = JacksonUtils
        .getReader()
        .readTree("""
          |{
          |  "source": "/home/alice/image.iso",
          |  "destination": "/mnt/storage",
          |  "chunks": {
          |    "size": 1024
          |  }
          |}
        """.stripMargin)
      val doc = JsonDocument(json)

      ZIO.serviceWithZIO[JsonSchemaValidator] { validator =>
        for {
          file <- acquire("server/src/test/resources/config-schema.json")
          configStr = file.getLines().mkString
          spec      = JacksonUtils.getReader().readTree(configStr)
          schema    = JsonSchema(uri = SchemaId("schema-1"), spec = spec)
          result <- validator.validate(doc, schema)
        } yield assertTrue(result == Right(()))
      }
    }

  def testFailure =
    test("validate failure") {
      val json = JacksonUtils
        .getReader()
        .readTree("""
          |{
          |  "source": "/home/alice/image.iso",
          |  "chunks": {
          |    "size": 1024
          |  }
          |}
        """.stripMargin)
      val doc = JsonDocument(json)

      ZIO.serviceWithZIO[JsonSchemaValidator] { validator =>
        for {
          file <- acquire("server/src/test/resources/config-schema.json")
          configStr = file.getLines().mkString
          spec      = JacksonUtils.getReader().readTree(configStr)
          schema    = JsonSchema(uri = SchemaId("schema-1"), spec = spec)
          result <- validator.validate(doc, schema)
          error = "object has missing required properties ([\"destination\"])"
        } yield assertTrue(result == Left(error))
      }
    }

  def acquire(name: => String): ZIO[Any with Scope, IOException, BufferedSource] =
    ZIO.acquireRelease(
      ZIO.attemptBlockingIO(Source.fromFile(name))
    )(source => ZIO.attempt(source.close()).ignore)
}
