import zio._

import app.HttpServer
import infra.Layers
import scala.io.StdIn

object App extends ZIOAppDefault {

  override val run = app
    .onExit { cleanup =>
      ZIO.logWarning(s"Termianting server") *>
        cleanup.flatMapExitZIO { server => server.interrupt }
    }
    .catchAll { e => ZIO.logError(s"App Stopped: ${e.getMessage}") }
    .provide(Layers.logger, Scope.default)

  private def app: ZIO[Any with Scope, Throwable, Fiber.Runtime[Throwable, Nothing]] =
    for {
      _ <- ZIO.logInfo("App Started")
      hostname = "0.0.0.0"
      port     = 8080
      _ <- ZIO.logInfo(
        s"Starting server to listen on port: http://$hostname:$port/api/health"
      )
      running <- HttpServer.run(hostname, port)
      _       <- ZIO.never
    } yield running
}
