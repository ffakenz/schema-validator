package app.json

import algebra.SchemaF
import app.ServiceResponse.ErrorResponse._
import app.ServiceResponse._
import com.fasterxml.jackson.core.JsonParseException
import com.github.fge.jackson.JacksonUtils
import model.json.{ JSON, JsonSchema, SchemaId }
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import sttp.tapir.ztapir._
import zio.{ ZIO, _ }

object UploadRoutes {

  private val inputExample =
    """
      |{
      |  "$schema": "http://json-schema.org/draft-04/schema#",
      |  "type": "object",
      |  "properties": {
      |    "source": {
      |      "type": "string"
      |    },
      |    "destination": {
      |      "type": "string"
      |    },
      |    "timeout": {
      |      "type": "integer",
      |      "minimum": 0,
      |      "maximum": 32767
      |    },
      |    "chunks": {
      |      "type": "object",
      |      "properties": {
      |        "size": {
      |          "type": "integer"
      |        },
      |        "number": {
      |          "type": "integer"
      |        }
      |      },
      |      "required": ["size"]
      |    }
      |  },
      |  "required": ["source", "destination"]
      |}
      |""".stripMargin

  private val uploadEndpoint = {
    endpoint
      .name("Upload a JSON Schema with unique `SCHEMAID`")
      .post
      .in("schema")
      .in(path[String]("schemaId").default("schema-id"))
      .in(stringJsonBody.example(inputExample))
      .out(jsonBody[SuccessResponse])
      .out(statusCode(StatusCode.Created))
      .errorOut(
        oneOf[ErrorResponse](
          oneOfVariant(jsonBody[OkErrorResponse] and statusCode(StatusCode.Ok)),
          oneOfVariant(jsonBody[BadRequestErrorResponse] and statusCode(StatusCode.BadRequest)),
          oneOfVariant(jsonBody[InternalErrorResponse] and statusCode(StatusCode.InternalServerError))
        )
      )
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
          .catchAll {
            case e: IllegalArgumentException =>
              ZIO.logError(
                s"Error in action $actionName for schema $schemaId: ${e.getMessage}"
              ) *>
                ZIO.succeed(Left(BadRequestErrorResponse(actionName, schemaId, message = e.getMessage)))
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
