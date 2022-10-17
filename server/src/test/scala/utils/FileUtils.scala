package utils

import zio.{ Scope, ZIO }

import java.io.IOException
import scala.io.{ BufferedSource, Source }

object FileUtils {

  def acquire(name: => String): ZIO[Any with Scope, IOException, BufferedSource] =
    ZIO.acquireRelease(
      ZIO.attemptBlockingIO(Source.fromFile(name))
    )(source => ZIO.attempt(source.close()).ignore)
}
