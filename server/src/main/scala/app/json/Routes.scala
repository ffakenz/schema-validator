package app.json

import zio.ZIO
import zio.Tag
import zhttp.http.{ Http, Method, Request, Response, Status, !!, /, -> }
import com.github.fge.jackson.JacksonUtils
import model.json.{ JSON, JsonDocument, JsonSchema, SchemaId }
import app.ServiceResposne._
import zio.json._

object Routes {

  def apply(): Http[algebra.SchemaF[JSON], Throwable, Request, Response] =
    Http
      .fromZIO(ZIO.service[algebra.SchemaF[JSON]])
      .flatMap { algebra =>
        Http.collectZIO[Request] {

          case req @ (Method.POST -> !! / "schema" / schemaId) =>
            val action = "upload"
            handleRequest(req)(e =>
              Response
                .json(ErrorResponse(action, "schemaId", e.getMessage).toJson)
                .setStatus(Status.BadRequest)
            ) { spec =>
              val uri    = SchemaId(schemaId)
              val schema = JsonSchema(uri = uri, spec = spec)
              for {
                _ <- algebra.upload(schema)
              } yield Response.json(SuccessResponse(action, schemaId).toJson)
            }

          case Method.GET -> !! / "schema" / schemaId =>
            val uri = SchemaId(schemaId)
            algebra.download(uri).map {
              case Some(schema) =>
                Response.json(schema.spec.toPrettyString)
              case None =>
                Response.status(Status.NotFound)
            }

          case req @ (Method.POST -> !! / "validate" / schemaId) =>
            val action = "validate"
            handleRequest(req)(e =>
              Response
                .json(ErrorResponse(action, "schemaId", e.getMessage).toJson)
                .setStatus(Status.BadRequest)
            ) { json =>
              val doc = JsonDocument(json)
              val uri = SchemaId(schemaId)
              for {
                result <- algebra.validate(uri, doc)
              } yield result match {
                case Left(error) =>
                  Response.json(ErrorResponse(action, schemaId, error).toJson)
                case Right(_) =>
                  Response.json(SuccessResponse(action, schemaId).toJson)
              }
            }
        }
      }

  private def handleRequest(req: Request)(
      errorHandler: Throwable => Response
  )(
      callback: JSON => ZIO[Any, Throwable, Response]
  ): ZIO[Any, Throwable, Response] =
    (for {
      jsonStr <- req.body.asString
      spec    <- ZIO.attempt(JacksonUtils.getReader().readTree(jsonStr))
      response <- ZIO.when(spec.isObject())(callback(spec)).map {
        case Some(resp) => resp
        case None       => Response.status(Status.BadRequest)
      }
    } yield response)
      .catchAll {
        case e: NoSuchElementException   => ZIO.succeed(Response.status(Status.NotFound))
        case e: IllegalArgumentException => ZIO.succeed(Response.status(Status.BadRequest))
        case e                           => ZIO.succeed(errorHandler(e))
      }
}
