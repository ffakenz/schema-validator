package service.impl

import zio.{ ZIO, Ref }
import zio.test.{ test, _ }
import model.json._
import zio.ZLayer
import zio.ZEnvironment
import model.domain._
import zio.UIO
import service.impl.JsonSchemaValidator

object JsonSchemaValidatorSuite {

  def jsonSuite =
    suite("JSON Suite")(
      testSuccess,
      testFailure
    ).provide(JsonSchemaValidator.layer)

  def testSuccess =
    test("validate success") {
      val jsonSchema =
        JsonSchema(uri = SchemaId("schema-1"), spec = JsonSchemaSpec("spec-1"))

      val jsonDocument =
        JsonDocument("json")

      ZIO.serviceWithZIO[JsonSchemaValidator] { validator =>
        for {
          result <- validator.validate(jsonDocument, jsonSchema)
        } yield assertTrue(result == Right(()))
      }
    }

  def testFailure =
    test("validate failure") {
      val jsonSchema =
        JsonSchema(uri = SchemaId("schema-1"), spec = JsonSchemaSpec("spec-1"))

      val jsonDocument =
        JsonDocument("spec-2")

      ZIO.serviceWithZIO[JsonSchemaValidator] { validator =>
        for {
          result <- validator.validate(jsonDocument, jsonSchema)
        } yield assertTrue(result == Left("error"))
      }
    }
}
