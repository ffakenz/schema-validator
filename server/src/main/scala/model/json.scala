package model

import com.fasterxml.jackson.databind.JsonNode
import model.domain._

object json {

  type JSON = JsonNode

  case class SchemaId(
      schemaURI: String
  ) extends URI

  case class JsonDocument(value: JSON) extends Document[JSON]

  case class JsonSchema(uri: SchemaId, spec: JSON) extends Schema[JSON]
}
