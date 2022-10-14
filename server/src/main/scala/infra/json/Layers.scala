package infra.json

import zio._
import zio.logging.backend.SLF4J
import zio.logging.LogFormat
import infra.json.JsonSchemaRegistry
import service.json.{ JsonDocumentCleaner, JsonSchemaValidator }
import infra.json.JacksonValidorClient

object Layers {
  val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)

  val schemaRegistry = JsonSchemaRegistry.live

  val schemaValidator = JsonSchemaValidator.live

  val documentCleaner = JsonDocumentCleaner.live

  val jsonValidator = JacksonValidorClient.live
}
