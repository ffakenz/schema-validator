package app.json

import algebra.SchemaF
import model.domain
import model.json.{ JSON, SchemaId }
import sttp.model.StatusCode
import sttp.tapir.ztapir._
import zio.{ ZIO, _ }

object DownloadRoutes {

  private val downloadEndpoint =
    endpoint
      .name("Download a JSON Schema with unique `SCHEMAID`")
      .get
      .in("schema")
      .in(path[String]("schemaId").default("schema-id"))
      .in(emptyInput)
      .out(stringJsonBody)
      .errorOut(emptyOutput and statusCode(StatusCode.NotFound))

  val downloadServerEndpoint = {
    val actionName = "download"
    downloadEndpoint
      .serverLogic { case schemaId =>
        val action =
          ZIO.logInfo(s"Downloading schema $schemaId") *>
            download(schemaId)
        val result: ZIO[SchemaF[JSON], Throwable, Either[Unit, String]] =
          action
            .map {
              case None         => Left(())
              case Some(schema) => Right(schema.spec.toPrettyString)
            }
        result
      }
  }

  private def download: String => RIO[
    SchemaF[JSON],
    Option[domain.Schema[JSON]]
  ] = { case schemaId =>
    val uri = SchemaId(schemaId)
    for {
      algebra <- ZIO.service[algebra.SchemaF[JSON]]
      result  <- algebra.download(uri)
    } yield result
  }
}
