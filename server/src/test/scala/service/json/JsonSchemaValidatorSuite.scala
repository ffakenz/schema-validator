package service.json

import com.github.fge.jackson.JacksonUtils
import infra.json.JacksonValidorClient
import model.json._
import utils.FileUtils.acquire
import zio.{ Scope, ZIO }
import zio.test.{ test, _ }

object JsonSchemaValidatorSuite {

  def jsonSuite =
    suite("JSON Suite")(
      testSuccess,
      testFailure
    ).provide(
      JacksonValidorClient.live,
      JsonSchemaValidator.live,
      Scope.default
    )

  def testSuccess =
    test("validate success") {
      val json = JacksonUtils.getReader
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
          configStr = file.getLines.mkString
          spec      = JacksonUtils.getReader.readTree(configStr)
          schema    = JsonSchema(uri = SchemaId("schema-1"), spec = spec)
          result <- validator.validate(doc, schema)
        } yield assertTrue(result == Right(()))
      }
    }

  def testFailure =
    test("validate failure") {
      val json = JacksonUtils.getReader
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
          configStr = file.getLines.mkString
          spec      = JacksonUtils.getReader.readTree(configStr)
          schema    = JsonSchema(uri = SchemaId("schema-1"), spec = spec)
          result <- validator.validate(doc, schema)
          error = "[ [0]: object has missing required properties ([\"destination\"]) ]"
        } yield assertTrue(result == Left(error))
      }
    }
}
