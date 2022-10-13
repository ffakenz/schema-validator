package app

import zio.ZLayer
import zhttp.service.Server
import zio.ZIO
import zio.{ Fiber, Scope }

object HttpServer {

  def run(
      hostname: String,
      port: Int
  ): ZIO[Any with Scope, Nothing, Fiber.Runtime[Throwable, Nothing]] =
    for {
      fiber <- serverSetup(hostname, port).startDefault
        .provide(
          ZLayer.succeed("api")
        )
        .forkScoped
        .interruptible
    } yield fiber

  private def serverSetup(
      hostname: String,
      port: Int
  ): Server[String, Throwable] =
    Server.bind(hostname, port) ++
      Server.app(
        Routes() @@ MiddleWares()
      )
}
