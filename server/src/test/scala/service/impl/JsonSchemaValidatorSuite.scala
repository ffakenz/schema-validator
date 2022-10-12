package service.impl

import zio.{ ZIO, Ref }
import zio.test.{ test, _ }
import model.json._
import zio.ZLayer
import zio.ZEnvironment
import model.domain._
import zio.UIO
import service.impl.JsonSchemaValidator
import infra.Layers
import com.github.fge.jackson.JacksonUtils
import java.io.IOException
import scala.io.Source

object JsonSchemaValidatorSuite {

  def jsonSuite =
    suite("JSON Suite")(
      testSuccess,
      testFailure
    ).provide(
      Layers.jsonValidator,
      Layers.jsonValidatorClient,
      JsonSchemaValidator.layer
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
          configStr <- acquire("server/src/test/resources/config-schema.json")
          spec   = JacksonUtils.getReader().readTree(configStr)
          schema = JsonSchema(uri = SchemaId("schema-1"), spec = spec)
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
          configStr <- acquire("server/src/test/resources/config-schema.json")
          spec   = JacksonUtils.getReader().readTree(configStr)
          schema = JsonSchema(uri = SchemaId("schema-1"), spec = spec)
          result <- validator.validate(doc, schema)
          error = "object has missing required properties ([\"destination\"])"
        } yield assertTrue(result == Left(error))
      }
    }

  def acquire(name: => String): ZIO[Any, IOException, String] =
    ZIO.attemptBlockingIO(Source.fromFile(name).getLines().mkString)
}
