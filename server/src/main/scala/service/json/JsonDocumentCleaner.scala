package service.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jackson.JacksonUtils
import model.domain.Document
import model.json.{ JSON, JsonDocument }
import service.DocumentCleaner
import zio.{ Task, ZIO, ZLayer }

import scala.jdk.CollectionConverters._

case class JsonDocumentCleaner() extends DocumentCleaner[JSON, Task] {

  def clean(document: Document[JSON]): Task[Document[JSON]] = {
    ZIO.attempt(cleanUnsafe(document))
  }

  private def cleanUnsafe(document: Document[JSON]): Document[JSON] = {
    val json = document.value

    if (json.isArray) {
      cleanArrayUnsafe(document)
    } else if (json.isObject) {
      cleanObjectUnsafe(document)
    } else {
      document
    }
  }

  private def cleanArrayUnsafe(document: Document[JSON]): Document[JSON] = {
    val mapper = new ObjectMapper
    val json   = document.value
    val values = json.elements.asScala.toList
    val cleanDoc = values
      .foldLeft(mapper.createArrayNode()) { case (acc, value) =>
        if (value.isNull) {
          acc
        } else {
          val emptyJson = JacksonUtils.getReader.readTree("{}")
          val clean     = cleanUnsafe(JsonDocument(value)).value
          if (clean.equals(emptyJson) || clean.isNull) acc
          else acc.add(clean)
        }
      }
    JsonDocument(cleanDoc)
  }

  private def cleanObjectUnsafe(document: Document[JSON]): Document[JSON] = {
    val mapper    = new ObjectMapper
    val emptyJson = JacksonUtils.getReader.readTree("{}")
    val json      = document.value
    val keys      = json.fieldNames.asScala.toList
    val values    = json.elements.asScala.toList

    val cleanDoc = (keys zip values)
      .foldLeft(mapper.createObjectNode()) { case (acc, (key, value)) =>
        if (value.equals(emptyJson) || value.isNull) acc
        else if (value.elements.asScala.nonEmpty) {
          val clean = cleanUnsafe(JsonDocument(value)).value
          if (clean.equals(emptyJson) || clean.isNull) acc
          else acc.set(key, clean)
        } else acc.set(key, value)
      }
    JsonDocument(cleanDoc)
  }
}

object JsonDocumentCleaner {

  val live: ZLayer[Any, Nothing, JsonDocumentCleaner] =
    ZLayer {
      ZIO.succeed(JsonDocumentCleaner())
    }
}
