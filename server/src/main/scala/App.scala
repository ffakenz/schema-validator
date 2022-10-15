import app.HttpServer
import infra.json.Layers
import zio._
import app.HttpServerConfig

object App extends ZIOAppDefault {

  override val run = app
    .catchAll { e => ZIO.logError(s"App Stopped: ${e.getMessage}") }
    .provide(Layers.logger)

  private def app: Task[Unit] =
    (for {
      _    <- ZIO.logInfo("App Started")
      conf <- ZIO.service[HttpServerConfig]
      hostname = conf.hostname
      port     = conf.port
      _ <- ZIO.logInfo(
        s"Starting server to listen on port: http://$hostname:$port/api/health"
      )
      _ <- HttpServer.run(hostname, port)
      _ <- ZIO.never
    } yield ()).provide(HttpServerConfig.live)

}
