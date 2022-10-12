package service.impl

import model.domain.{ Document, Schema, URI }
import model.json.JSON
import zio.ZIO
import zio.ZLayer
import JsonSchemaValidator.Z
import model.domain
import service.SchemaValidator
import infra.impl.JsonValidorClient
import com.github.fge.jsonschema.main.JsonValidator

// @TODO use json-schema-validator lib
case class JsonSchemaValidator() extends SchemaValidator[JSON, Z] {

  def validate(
      document: Document[JSON],
      schema: Schema[JSON]
  ): Z[Either[String, Unit]] =
    ZIO.serviceWithZIO[JsonValidorClient] { validator =>
      validator.validate("schema.spec", null, true)
    }
}

object JsonSchemaValidator {
  type Z[A] = ZIO[JsonValidator with JsonValidorClient, Throwable, A]

  val layer: ZLayer[JsonValidorClient, Nothing, JsonSchemaValidator] =
    ZLayer {
      ZIO.succeed(JsonSchemaValidator())
    }
}
