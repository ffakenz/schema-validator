package service.json

import model.domain.{ Document, Schema, URI }
import model.json.JSON
import zio.{ Task, ZIO }
import zio.ZLayer
import model.domain
import service.SchemaValidator
import infra.json.JacksonValidorClient
import com.github.fge.jsonschema.main.JsonValidator

// @TODO use json-schema-validator lib
case class JsonSchemaValidator(
    validator: JacksonValidorClient
) extends SchemaValidator[JSON, Task] {

  def validate(
      document: Document[JSON],
      schema: Schema[JSON]
  ): Task[Either[String, Unit]] =
    validator.validate(schema.spec, document.value, true)
}

object JsonSchemaValidator {
  type Dependencies = JacksonValidorClient

  val live: ZLayer[Dependencies, Nothing, JsonSchemaValidator] =
    ZLayer {
      for {
        validator <- ZIO.service[JacksonValidorClient]
      } yield JsonSchemaValidator(validator)
    }
}
