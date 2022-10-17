package app.json

import algebra.SchemaF
import app.ServiceResponse._
import com.fasterxml.jackson.core.JsonParseException
import com.github.fge.jackson.JacksonUtils
import model.domain
import sttp.tapir.json.zio._
import sttp.tapir.ztapir._
import model.json.{ JSON, JsonDocument, JsonSchema, SchemaId }
import zio.{ ZIO, _ }
import sttp.tapir.generic.auto._
import sttp.model.StatusCode

object UploadRoutes {

  private val uploadEndpoint = {
    endpoint
      .name("Upload a JSON Schema with unique `SCHEMAID`")
      .post
      .in("schema")
      .in(path[String]("schemaId"))
      .in(stringJsonBody)
      .out(jsonBody[SuccessResponse])
      .out(statusCode(StatusCode.Created))
      .errorOut(jsonBody[ErrorResponse] and statusCode(StatusCode.BadRequest))
  }

  val uploadServerEndpoint = {
    val actionName = "upload"
    uploadEndpoint.serverLogic { case (schemaId, jsonStr) =>
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
            case e: JsonParseException =>
              ZIO.logError(
                s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
              ) *>
                ZIO.succeed(Left(ErrorResponse(actionName, schemaId, "Invalid JSON")))
            case e =>
              ZIO.logError(
                s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
              ) *>
                ZIO.succeed(Left(ErrorResponse(actionName, schemaId, e.getMessage)))
          }
      result
    }
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
}
