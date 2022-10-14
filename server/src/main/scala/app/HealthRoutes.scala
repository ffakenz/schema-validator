package app

import zhttp.http.{ Http, Method, Request, Response, !!, /, -> }

object HealthRoutes {

  def apply(): Http[Any, Nothing, Request, Response] =
    Http.collect[Request] { case Method.GET -> !! / "ready" =>
      Response.ok
    }
}
