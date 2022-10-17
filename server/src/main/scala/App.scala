import app.{ HttpServer, HttpServerConfig }
import infra.json.Layers
import zio._

object App extends ZIOAppDefault {

  override val run = app
    .catchAll { e => ZIO.logError(s"App Stopped: ${e.getMessage}") }
    .provide(Layers.logger, HttpServerConfig.live)

  private def app: RIO[HttpServerConfig, Unit] =
    for {
      _    <- ZIO.logInfo("App Started")
      conf <- ZIO.service[HttpServerConfig]
      hostname = conf.hostname
      port     = conf.port
      _ <- ZIO.logInfo(s"Starting server to listen on: http://$hostname:$port/")
      _ <- HttpServer.run(hostname, port)
      _ <- ZIO.logInfo(s"Swagger-UI available at: http://$hostname:$port/docs")
      _ <- ZIO.never
    } yield ()

}
