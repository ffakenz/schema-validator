package app.json

import zio.ZIO
import zio.Tag
import zhttp.http.{ Http, Method, Request, Response, Status, !!, /, -> }
import com.github.fge.jackson.JacksonUtils
import model.json.{ JSON, JsonDocument, JsonSchema, SchemaId }

object Routes {

  def apply(): Http[String with algebra.SchemaF[JSON], Throwable, Request, Response] =
    Http
      .fromZIO(
        for {
          root    <- ZIO.service[String]
          algebra <- ZIO.service[algebra.SchemaF[JSON]]
        } yield (root, algebra)
      )
      .flatMap { case (api, algebra) =>
        Http.collectZIO[Request] {
          case req @ (Method.POST -> !! / `api` / "schema" / schemaId) =>
            for {
              // test: bad-request
              jsonStr <- req.body.asString
              spec    <- ZIO.attempt(JacksonUtils.getReader().readTree(jsonStr))
              uri    = SchemaId(schemaId)
              schema = JsonSchema(uri = uri, spec = spec)
              _ <- algebra.upload(schema)
            } yield Response.json("{}")

          case Method.GET -> !! / `api` / "schema" / schemaId =>
            val uri = SchemaId(schemaId)
            algebra.download(uri).map {
              case Some(schema) => Response.json("{}")
              case None         => Response.json("{}").setStatus(Status.NotFound)
            }

          case req @ (Method.POST -> !! / `api` / "validate" / schemaId) =>
            for {
              // test: bad-request
              jsonStr <- req.body.asString
              json    <- ZIO.attempt(JacksonUtils.getReader().readTree(jsonStr))
              doc = JsonDocument(json)
              uri = SchemaId(schemaId)
              result <- algebra.validate(uri, doc)
            } yield result match {
              case Left(error) => Response.json("{}").setStatus(Status.BadRequest)
              case Right(_)    => Response.json("{}")
            }

          case Method.GET -> !! / `api` / "ready" =>
            ZIO.succeed(Response.text(s"Service Ready"))

          case Method.GET -> !! / `api` / "health" =>
            ZIO.succeed(Response.text(s"Service Healthy"))
        }
      }
}
