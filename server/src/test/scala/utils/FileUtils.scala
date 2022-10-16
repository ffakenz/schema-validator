package utils

import zio.{ ZIO, Scope }
import scala.io.{ Source, BufferedSource }
import java.io.IOException

object FileUtils {

  def acquire(name: => String): ZIO[Any with Scope, IOException, BufferedSource] =
    ZIO.acquireRelease(
      ZIO.attemptBlockingIO(Source.fromFile(name))
    )(source => ZIO.attempt(source.close()).ignore)
}
