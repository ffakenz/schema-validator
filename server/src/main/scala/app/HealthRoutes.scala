package app

import zhttp.http._

object HealthRoutes {

  def apply(): Http[Any, Nothing, Request, Response] =
    Http.collect[Request] { case Method.GET -> !! / "ready" =>
      Response.ok
    }
}
