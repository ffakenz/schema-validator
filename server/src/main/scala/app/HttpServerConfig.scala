package app

import zio._
import zio.config._, ConfigDescriptor._
import zio.config.typesafe._

case class HttpServerConfig(
    hostname: String,
    port: Int
)

object HttpServerConfig {

  private val descriptor: ConfigDescriptor[HttpServerConfig] =
    nested("http-server")(
      (string("hostname") <*> int("port"))(
        { case (hostname, port) => HttpServerConfig(hostname, port) },
        config => Some(config.hostname, config.port)
      )
    )

  val live = ZLayer.fromZIO {
    read[HttpServerConfig](
      HttpServerConfig.descriptor from
        ConfigSource.fromResourcePath
    )
  }
}
