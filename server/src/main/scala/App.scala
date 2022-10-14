import zio._

import app.HttpServer
import infra.Layers
import scala.io.StdIn

object App extends ZIOAppDefault {

  override val run = app
    .catchAll { e => ZIO.logError(s"App Stopped: ${e.getMessage}") }
    .provide(Layers.logger)

  private def app: Task[Unit] =
    for {
      _ <- ZIO.logInfo("App Started")
      hostname = "0.0.0.0"
      port     = 8080
      _ <- ZIO.logInfo(
        s"Starting server to listen on port: http://$hostname:$port/api/health"
      )
      _ <- HttpServer.run(hostname, port)
      _ <- ZIO.never
    } yield ()
}
