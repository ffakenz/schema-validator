package app

import zio.ZLayer
import zhttp.service.Server
import zio.ZIO
import zio.{ Fiber, Scope }

object HttpServer {

  def run(hostname: String, port: Int) =
    serverSetup(hostname, port).startDefault
      .provide(
        ZLayer.succeed("api"),
        infra.json.Layers.schemaRegistry,
        infra.json.Layers.schemaValidator,
        infra.json.Layers.documentCleaner,
        infra.json.Layers.jsonValidator,
        algebra.SchemaF.layer[model.json.JSON]
      )
      .fork

  private def serverSetup(hostname: String, port: Int) =
    Server.bind(hostname, port) ++
      Server.app(
        app.json.Routes() @@ MiddleWares()
      )
}
