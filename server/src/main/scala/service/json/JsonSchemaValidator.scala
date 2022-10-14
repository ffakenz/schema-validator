package service.json

import model.domain.{ Document, Schema, URI }
import model.json.JSON
import zio.ZIO
import zio.ZLayer
import JsonSchemaValidator.Z
import model.domain
import service.SchemaValidator
import infra.json.JacksonValidorClient
import com.github.fge.jsonschema.main.JsonValidator

// @TODO use json-schema-validator lib
case class JsonSchemaValidator() extends SchemaValidator[JSON, Z] {

  def validate(
      document: Document[JSON],
      schema: Schema[JSON]
  ): Z[Either[String, Unit]] =
    ZIO.serviceWithZIO[JacksonValidorClient] { validator =>
      validator.validate(schema.spec, document.value, true)
    }
}

object JsonSchemaValidator {
  type Z[A] = ZIO[JsonValidator with JacksonValidorClient, Throwable, A]

  val layer: ZLayer[JacksonValidorClient, Nothing, JsonSchemaValidator] =
    ZLayer {
      ZIO.succeed(JsonSchemaValidator())
    }
}
