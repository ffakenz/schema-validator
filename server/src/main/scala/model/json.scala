package model

import domain._

object json {

  // @TODO use zio-json
  type JSON = String

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
