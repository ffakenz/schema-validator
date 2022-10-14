package infra.json

import service.json.{ JsonDocumentCleaner, JsonSchemaValidator }
import zio._
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Layers {

  val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)

  val schemaRegistry = JsonSchemaRegistry.live

  val schemaValidator = JsonSchemaValidator.live

  val documentCleaner = JsonDocumentCleaner.live

  val jsonValidator = JacksonValidorClient.live
}
