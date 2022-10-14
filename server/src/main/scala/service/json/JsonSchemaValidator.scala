package service.json

import infra.json.JacksonValidorClient
import model.domain.{ Document, Schema }
import model.json.JSON
import service.SchemaValidator
import zio.{ Task, ZIO, ZLayer }

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
