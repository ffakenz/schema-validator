package service.impl

import model.domain.{ Document, Schema, URI }
import model.json.JSON
import zio.ZIO
import zio.ZLayer
import JsonDocumentCleaner.Z
import model.domain
import service.DocumentCleaner

case class JsonDocumentCleaner() extends DocumentCleaner[JSON, Z] {

  def clean(document: Document[JSON]): Z[Document[JSON]] =
    ZIO.succeed(document)

}

object JsonDocumentCleaner {
  type Z[A] = ZIO[Any, Throwable, A]

  val layer: ZLayer[Any, Nothing, JsonDocumentCleaner] =
    ZLayer {
      ZIO.succeed(JsonDocumentCleaner())
    }
}
