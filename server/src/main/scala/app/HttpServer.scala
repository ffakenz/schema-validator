package app

import zio.ZLayer
import zhttp.service.Server
import zio.ZIO
import zio.{ Fiber, Scope }

object HttpServer {

  def run(hostname: String, port: Int): ZIO[Any, Throwable, Unit] =
    for {
      _ <- serverSetup(hostname: String, port).startDefault
        .provide(
          ZLayer.succeed("api")
        )
        .fork
        .flatMap(_.join)
    } yield ()

  private def serverSetup(hostname: String, port: Int): Server[String, Throwable] =
    Server.bind(hostname, port) ++
      Server.app(
        Routes() @@ MiddleWares()
      )
}
