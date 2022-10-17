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

object ValidateRoutes {

  private val validateEndpoint =
    endpoint
      .name("Validate a JSON document against the JSON Schema identified by `SCHEMAID`")
      .post
      .in("validate")
      .in(path[String]("schemaId"))
      .in(stringJsonBody)
      .out(jsonBody[SuccessResponse])
      .out(statusCode(StatusCode.Ok))
      .errorOut(jsonBody[ErrorResponse] and statusCode(StatusCode.BadRequest))

  val validateServerEndpoint = {
    val actionName = "validate"
    validateEndpoint.serverLogic { case (schemaId, jsonStr) =>
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