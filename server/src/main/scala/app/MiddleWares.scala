package app

import zio.ZIO
import zhttp.http.{ Http, HttpApp, Middleware, Request, Response, Status }
import zhttp.http.middleware.HttpMiddleware

import java.io.IOException

object MiddleWares {

  // catches errors and stops the default render of stack trace
  private val errorMiddleware = new HttpMiddleware[Any, Throwable] {
    override def apply[
        R1 <: Any,
        E1 >: Throwable
    ](http: HttpApp[R1, E1]) = http
      .catchAll { ex =>
        val zio: ZIO[Any, IOException, Response] = for {
          _ <- ZIO.logError(ex.toString)
        } yield Response.status(Status.InternalServerError)
        Http.responseZIO(zio)
      }
  }

  def apply(): Middleware[Any, Throwable, Request, Response, Request, Response] =
    errorMiddleware ++ Middleware.dropTrailingSlash
}