package service.impl

import model.domain.{ Document, Schema, URI }
import model.json.JSON
import zio.ZIO
import zio.ZLayer
import JsonDocumentCleaner.Z
import model.domain
import service.DocumentCleaner
import scala.jdk.CollectionConverters._
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import scala.util.Try
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.main.JsonValidator
import model.json.JsonDocument

case class JsonDocumentCleaner() extends DocumentCleaner[JSON, Z] {

  def clean(document: Document[JSON]): Z[Document[JSON]] = {
    ZIO.fromTry(
      Try {
        val mapper = new ObjectMapper()
        val keys   = document.value.fieldNames.asScala.toList
        val values = document.value.elements.asScala.toList

        val cleanDoc =
          (keys zip values)
            .foldLeft(mapper.createObjectNode()) { case (acc, (key, value)) =>
              if (!value.isNull()) acc.set(key, value)
              else acc
            }
        JsonDocument(cleanDoc)
      }
    )
  }
}

object JsonDocumentCleaner {
  type Z[A] = ZIO[Any, Throwable, A]

  val jsonValidator: ZLayer[Any, Throwable, JsonValidator] = ZLayer {
    ZIO.fromTry(
      Try {
        JsonSchemaFactory.byDefault().getValidator()
      }
    )
  }

  val layer: ZLayer[Any, Nothing, JsonDocumentCleaner] =
    ZLayer {
      ZIO.succeed(JsonDocumentCleaner())
    }
}
