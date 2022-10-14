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
      .fromZIO(
        for {
          algebra <- ZIO.service[algebra.SchemaF[JSON]]
        } yield algebra
      )
      .flatMap { algebra =>
        Http.collectZIO[Request] {
          case req @ (Method.POST -> !! / "schema" / schemaId) =>
            for {
              jsonStr <- req.body.asString
              spec    <- ZIO.attempt(JacksonUtils.getReader().readTree(jsonStr)) // 400
              uri    = SchemaId(schemaId)
              schema = JsonSchema(uri = uri, spec = spec)
              _ <- algebra.upload(schema)
            } yield Response.json(SuccessResponse("upload", schemaId).toJson)

          case Method.GET -> !! / "schema" / schemaId =>
            val uri = SchemaId(schemaId)
            algebra.download(uri).map {
              case Some(schema) =>
                Response.json(schema.spec.toPrettyString)
              case None =>
                Response.status(Status.NotFound)
            }

          case req @ (Method.POST -> !! / "validate" / schemaId) =>
            for {
              jsonStr <- req.body.asString
              json    <- ZIO.attempt(JacksonUtils.getReader().readTree(jsonStr)) // 400
              doc = JsonDocument(json)
              uri = SchemaId(schemaId)
              result <- algebra.validate(uri, doc)
            } yield result match {
              case Left(error) =>
                Response.json(ErrorResponse("validate", schemaId, error).toJson)
              case Right(_) =>
                Response.json(SuccessResponse("validate", schemaId).toJson)
            }

          case Method.GET -> !! / "ready" =>
            ZIO.succeed(Response.text(s"Service Ready"))

          case Method.GET -> !! / "health" =>
            ZIO.succeed(Response.text(s"Service Healthy"))
        }
      }
}
