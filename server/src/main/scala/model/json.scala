package model

import domain._
import com.fasterxml.jackson.databind.JsonNode

object json {

  type JSON = JsonNode

  // @TODO use URI
  case class SchemaId(
      schemaURI: String
  ) extends URI

  // @TODO use json-schema-validator lib
  case class JsonSchemaSpec(
      value: String
  ) extends SchemaSpec

  case class JsonDocument(value: JSON) extends Document[JSON]

  case class JsonSchema(uri: SchemaId, spec: JsonSchemaSpec) extends Schema[JSON]
}
