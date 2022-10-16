package app

import zhttp.service.Server

object HttpServer {

  def run(hostname: String, port: Int) =
    serverSetup(hostname, port).startDefault
      .provide(
        infra.json.Layers.schemaRegistry,
        infra.json.Layers.schemaValidator,
        infra.json.Layers.documentCleaner,
        infra.json.Layers.jsonValidator,
        algebra.SchemaF.layer[model.json.JSON]
      )
      .fork

  private def routes() = HealthRoutes() ++ app.json.Routes()

  private def serverSetup(hostname: String, port: Int) =
    Server.bind(hostname, port) ++
      Server.app(
        routes() @@ MiddleWares()
      )
}
