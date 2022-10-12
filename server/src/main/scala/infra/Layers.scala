package infra

import zio._
import zio.logging.backend.SLF4J
import zio.logging.{ LogFormat, console }

object Layers {
  val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

}
