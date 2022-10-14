package infra

import zio._
import zio.logging.backend.SLF4J
import zio.logging.LogFormat
import infra.json.JsonSchemaRegistry
import service.json.{ JsonDocumentCleaner, JsonSchemaValidator }
import infra.json.JacksonValidorClient

object Layers {
  val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)

  val registry: ZLayer[Any, Nothing, JsonSchemaRegistry.Registry] = ZLayer(Ref.make(Map.empty))

  val schemaRegistry = JsonSchemaRegistry.layer

  val schemaValidator = JsonSchemaValidator.layer

  val documentCleaner = JsonDocumentCleaner.layer

  val jsonValidator = JacksonValidorClient.jsonValidator

  val jsonValidatorClient = JacksonValidorClient.jacksonClient
}
