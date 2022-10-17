package app.json

import algebra.SchemaF
import app.ServiceResponse._
import com.github.fge.jackson.JacksonUtils
import model.domain
import sttp.tapir.json.zio._
import sttp.tapir.ztapir._
import model.json.{ JSON, JsonDocument, JsonSchema, SchemaId }
import zio.{ ZIO, _ }
import sttp.tapir.generic.auto._
import sttp.model.StatusCode

object DownloadRoutes {

  private val downloadEndpoint =
    endpoint
      .name("Download a JSON Schema with unique `SCHEMAID`")
      .get
      .in("schema")
      .in(path[String]("schemaId"))
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
