package model

import domain._
import com.fasterxml.jackson.databind.JsonNode

object json {

  type JSON = JsonNode

  // @TODO use URI
  case class SchemaId(
      schemaURI: String
  ) extends URI

  case class JsonDocument(value: JSON) extends Document[JSON]

  case class JsonSchema(uri: SchemaId, spec: JSON) extends Schema[JSON]
}
