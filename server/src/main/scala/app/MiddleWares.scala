package app

import zhttp.http._
import zhttp.http.middleware.HttpMiddleware
import zio.ZIO

import java.io.IOException
import zhttp.http.middleware.Cors

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

  private val config: Cors.CorsConfig =
    Cors.CorsConfig(
      allowedOrigins = _ => true,
      allowedMethods = Some(Set(Method.GET, Method.POST))
    )

  def apply(): Middleware[Any, Throwable, Request, Response, Request, Response] =
    errorMiddleware ++ Middleware.dropTrailingSlash ++ Middleware.cors(config)
}
