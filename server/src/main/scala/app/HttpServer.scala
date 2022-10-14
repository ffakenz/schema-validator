package app

import zio.ZLayer
import zhttp.service.Server
import zio.ZIO
import zio.{ Fiber, Scope }

object HttpServer {

  def run(hostname: String, port: Int) =
    serverSetup(hostname, port).startDefault
      .provide(
        ZLayer.succeed("api")
      )
      .fork

  private def serverSetup(hostname: String, port: Int) =
    Server.bind(hostname, port) ++
      Server.app(
        Routes() @@ MiddleWares()
      )
}
