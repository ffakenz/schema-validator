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
      val spec = JacksonUtils.getReader().readTree("{}")
      val jsonSchema =
        JsonSchema(uri = SchemaId("schema-1"), spec = spec)

      val json         = JacksonUtils.getReader().readTree("{}")
      val jsonDocument = JsonDocument(json)

      ZIO.serviceWithZIO[JsonSchemaValidator] { validator =>
        for {
          result <- validator.validate(jsonDocument, jsonSchema)
        } yield assertTrue(result == Right(()))
      }
    }

  def testFailure =
    test("validate failure") {
      val spec = JacksonUtils.getReader().readTree("[]")
      val jsonSchema =
        JsonSchema(uri = SchemaId("schema-1"), spec = spec)

      val json         = JacksonUtils.getReader().readTree("{}")
      val jsonDocument = JsonDocument(json)

      ZIO.serviceWithZIO[JsonSchemaValidator] { validator =>
        for {
          result <- validator.validate(jsonDocument, jsonSchema)
          error = "JSON value is of type array, not a JSON Schema (expected an object)"
        } yield assertTrue(result == Left(error))
      }
    }
}
