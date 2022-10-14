package service.json

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
import com.github.fge.jackson.JacksonUtils

case class JsonDocumentCleaner() extends DocumentCleaner[JSON, Z] {

  def clean(document: Document[JSON]): Z[Document[JSON]] = {
    ZIO.fromTry(Try(cleanUnsafe(document)))
  }

  private def cleanUnsafe(document: Document[JSON]): Document[JSON] = {
    val json = document.value

    if (json.isArray()) {
      cleanArrayUnsafe(document)
    } else if (json.isObject()) {
      cleanObjectUnsafe(document)
    } else {
      document
    }
  }

  private def cleanArrayUnsafe(document: Document[JSON]): Document[JSON] = {
    val mapper = new ObjectMapper()
    val json   = document.value
    val values = json.elements.asScala.toList
    val cleanDoc = values
      .foldLeft(mapper.createArrayNode()) { case (acc, value) =>
        if (value.isNull()) {
          acc
        } else {
          val emptyJson = JacksonUtils.getReader().readTree("{}")
          val clean     = cleanUnsafe(JsonDocument(value)).value
          if (clean.equals(emptyJson) || clean.isNull()) acc
          else acc.add(clean)
        }
      }
    JsonDocument(cleanDoc)
  }

  private def cleanObjectUnsafe(document: Document[JSON]): Document[JSON] = {
    val mapper    = new ObjectMapper()
    val emptyJson = JacksonUtils.getReader().readTree("{}")
    val json      = document.value
    val keys      = json.fieldNames.asScala.toList
    val values    = json.elements.asScala.toList

    val cleanDoc = (keys zip values)
      .foldLeft(mapper.createObjectNode()) { case (acc, (key, value)) =>
        if (value.equals(emptyJson) || value.isNull()) acc
        else if (value.elements().asScala.nonEmpty) {
          val clean = cleanUnsafe(JsonDocument(value)).value
          if (clean.equals(emptyJson) || clean.isNull()) acc
          else acc.set(key, clean)
        } else acc.set(key, value)
      }
    JsonDocument(cleanDoc)
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
