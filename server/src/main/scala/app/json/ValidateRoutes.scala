package app.json

import algebra.SchemaF
import app.ServiceResponse.ErrorResponse._
import app.ServiceResponse._
import com.fasterxml.jackson.core.JsonParseException
import com.github.fge.jackson.JacksonUtils
import model.json.{ JSON, JsonDocument, SchemaId }
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.ztapir._
import zio.{ ZIO, _ }

object ValidateRoutes {

  private val inputExample =
    """
      |{
      |  "source": "/home/alice/image.iso",
      |  "destination": "/mnt/storage",
      |  "chunks": {
      |    "size": 1024
      |  }
      |}
      |""".stripMargin

  private val validateEndpoint =
    endpoint
      .tag("Document")
      .description("Validate a JSON document against the JSON Schema identified by `SCHEMAID`")
      .summary("Validate a Documents against a Schema")
      .post
      .in("validate")
      .in(path[String]("schemaId").default("schema-id"))
      .in(stringJsonBody.example(inputExample))
      .out(jsonBody[SuccessResponse])
      .out(statusCode(StatusCode.Ok))
      .errorOut(
        oneOf[ErrorResponse](
          oneOfVariant(jsonBody[OkErrorResponse] and statusCode(StatusCode.Ok)),
          oneOfVariant(jsonBody[BadRequestErrorResponse] and statusCode(StatusCode.BadRequest)),
          oneOfVariant(jsonBody[InternalErrorResponse] and statusCode(StatusCode.InternalServerError))
        )
      )

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
                ZIO.succeed(Left(OkErrorResponse(actionName, schemaId, message = error)))
            case Right(_) =>
              ZIO.succeed(Right(SuccessResponse(actionName, schemaId)))
          }
          .catchAll {
            case e: NoSuchElementException =>
              ZIO.logError(
                s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
              ) *>
                ZIO.succeed(Left(OkErrorResponse(actionName, schemaId, message = e.getMessage)))
            case e: JsonParseException =>
              ZIO.logError(
                s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
              ) *>
                ZIO.succeed(Left(BadRequestErrorResponse(actionName, schemaId, message = "Invalid JSON")))
            case e =>
              ZIO.logError(
                s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
              ) *>
                ZIO.succeed(Left(InternalErrorResponse(actionName, schemaId, message = e.getMessage)))
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
