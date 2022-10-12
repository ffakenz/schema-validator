package service.impl

import model.domain.{ Document, Schema, URI }
import model.json.JSON
import zio.ZIO
import zio.ZLayer
import JsonSchemaValidator.Z
import model.domain
import service.SchemaValidator

// @TODO use json-schema-validator lib
case class JsonSchemaValidator() extends SchemaValidator[JSON, Z] {

  def validate(
      document: Document[JSON],
      schema: Schema[JSON]
  ): Z[Either[String, Unit]] =
    if (document.value == "json")
      ZIO.succeed(Right())
    else
      ZIO.succeed(Left("error"))
}

object JsonSchemaValidator {
  type Z[A] = ZIO[Any, Throwable, A]

  val layer: ZLayer[Any, Nothing, JsonSchemaValidator] =
    ZLayer {
      ZIO.succeed(JsonSchemaValidator())
    }
}
