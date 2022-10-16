package app.json

import com.github.fge.jackson.JacksonUtils
import model.json.{ JSON, JsonDocument, JsonSchema, SchemaId }
import zio.ZIO
import zio.json._

import sttp.tapir.PublicEndpoint
import sttp.tapir.PublicEndpoint
import sttp.tapir.ztapir._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zhttp.http.{ Http, Request, Response }
import zio._
import app.ServiceResponse
import app.ServiceResponse._
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.generic.auto._
import sttp.model.StatusCode
import algebra.SchemaF
import sttp.tapir.json.zio._
import sttp.tapir.Schema
import model.domain
import sttp.tapir.server.interceptor.decodefailure.DecodeFailureHandler
import sttp.tapir.server.interceptor.DecodeFailureContext
import sttp.tapir.DecodeResult.Multiple
import sttp.tapir.DecodeResult
import sttp.tapir.DecodeResult.Missing
import sttp.tapir.DecodeResult.InvalidValue
import sttp.tapir.DecodeResult.Mismatch
import sttp.tapir.server.ziohttp.ZioHttpServerOptions

object Routes {

  private val uploadRoute = {
    val actionName = "upload"
    ZioHttpInterpreter().toHttp(
      endpoint
        .name("Upload a JSON Schema with unique `SCHEMAID`")
        .post
        .in("schema")
        .in(path[String]("schemaId"))
        .in(stringJsonBody)
        .out(jsonBody[SuccessResponse])
        .out(statusCode(StatusCode.Created))
        .errorOut(jsonBody[ErrorResponse] and statusCode(StatusCode.BadRequest))
        .serverLogic { case (schemaId, jsonStr) =>
          val action =
            ZIO.logInfo(s"Uploading schema $schemaId") *>
              upload(schemaId, jsonStr)
          val result: ZIO[SchemaF[JSON], Throwable, Either[ErrorResponse, SuccessResponse]] =
            action
              .map { _ => Right(SuccessResponse(actionName, schemaId)) }
              .catchSome {
                case e: IllegalArgumentException =>
                  ZIO.logError(
                    s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
                  ) *>
                    ZIO.succeed(Left(ErrorResponse(actionName, schemaId, e.getMessage)))

                case e =>
                  ZIO.logError(
                    s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
                  ) *>
                    ZIO.succeed(Left(ErrorResponse(actionName, schemaId, "Invalid JSON")))
              }
          result
        }
    )
  }

  private val downloadRoute = ZioHttpInterpreter().toHttp(
    endpoint
      .name("Download a JSON Schema with unique `SCHEMAID`")
      .get
      .in("schema")
      .in(path[String]("schemaId"))
      .in(emptyInput)
      .out(stringJsonBody)
      .errorOut(emptyOutput and statusCode(StatusCode.NotFound))
      .serverLogic { case schemaId =>
        val actionName = "download"
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
  )

  private val validateRoute = {
    val actionName = "validate"
    ZioHttpInterpreter().toHttp(
      endpoint
        .name("Validate a JSON document against the JSON Schema identified by `SCHEMAID`")
        .post
        .in("validate")
        .in(path[String]("schemaId"))
        .in(stringJsonBody)
        .out(jsonBody[SuccessResponse])
        .out(statusCode(StatusCode.Ok))
        .errorOut(
          jsonBody[ErrorResponse] and statusCode(StatusCode.BadRequest)
        )
        .serverLogic { case (schemaId, jsonStr) =>
          val action =
            ZIO.logInfo(s"Validating schema $schemaId") *>
              validate(schemaId, jsonStr)
          val result: ZIO[SchemaF[JSON], Throwable, Either[ErrorResponse, SuccessResponse]] =
            action
              .flatMap {
                case Left(error) =>
                  ZIO.logError(
                    s"Error in action $actionName for schema $schemaId: $error"
                  ) *>
                    ZIO.succeed(Left(ErrorResponse(actionName, schemaId, error)))
                case Right(_) =>
                  ZIO.succeed(Right(SuccessResponse(actionName, schemaId)))
              }
              .catchAll {
                case e: NoSuchElementException =>
                  ZIO.logError(
                    s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
                  ) *>
                    ZIO.succeed(Left(ErrorResponse(actionName, schemaId, e.getMessage)))
                case e =>
                  ZIO.logError(
                    s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
                  ) *>
                    ZIO.succeed(Left(ErrorResponse(actionName, schemaId, "Invalid JSON")))
              }
          result
        }
    )
  }

  def apply(): Http[SchemaF[JSON], Throwable, Request, Response] =
    downloadRoute ++ uploadRoute ++ validateRoute

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

  private def upload: ((String, String)) => RIO[
    SchemaF[JSON],
    Unit
  ] = { case (schemaId, jsonStr) =>
    for {
      algebra <- ZIO.service[algebra.SchemaF[JSON]]
      spec    <- ZIO.attempt(JacksonUtils.getReader.readTree(jsonStr))
      uri    = SchemaId(schemaId)
      schema = JsonSchema(uri = uri, spec = spec)
      response <- ZIO
        .when(spec.isObject)(
          algebra.upload(schema)
        )
        .flatMap {
          case Some(_) => ZIO.unit
          case None    => ZIO.fail(new IllegalArgumentException("Invalid JSON"))
        }
    } yield response
  }

  private def validate: (String, String) => RIO[
    SchemaF[JSON],
    Either[String, Unit]
  ] = { case (schemaId, jsonStr) =>
    for {
      algebra <- ZIO.service[algebra.SchemaF[JSON]]
      json    <- ZIO.attempt(JacksonUtils.getReader.readTree(jsonStr))
      uri = SchemaId(schemaId)
      doc = JsonDocument(json)
      response <- ZIO
        .when(json.isObject)(
          algebra.validate(uri, doc)
        )
        .flatMap {
          case Some(result) => ZIO.succeed(result)
          case None         => ZIO.fail(new IllegalArgumentException("Invalid JSON"))
        }
    } yield response
  }
}
