package infra

import zio._
import zio.logging.backend.SLF4J
import zio.logging.{ LogFormat, console }
import infra.impl.JsonSchemaRegistry
import service.impl.JsonSchemaValidator
import service.impl.JsonDocumentCleaner
import infra.impl.JacksonValidorClient

object Layers {
  val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val registry: ZLayer[Any, Nothing, JsonSchemaRegistry.Registry] =
    ZLayer(Ref.make(Map.empty))

  val schemaRegistry = JsonSchemaRegistry.layer

  val schemaValidator = JsonSchemaValidator.layer

  val documentCleaner = JsonDocumentCleaner.layer

  val jsonValidator       = JacksonValidorClient.jsonValidator
  val jsonValidatorClient = JacksonValidorClient.jacksonClient
}
