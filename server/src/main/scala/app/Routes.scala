package app

import zio.ZIO
import zhttp.http.{ Http, Method, Request, Response, !!, /, -> }

object Routes {

  def apply(): Http[String, Nothing, Request, Response] =
    Http.fromZIO(ZIO.service[String]).flatMap { api =>
      Http.collect[Request] {
        case Method.GET -> !! / `api` / "ready" =>
          Response.text(s"Service Ready")
        case Method.GET -> !! / `api` / "health" =>
          Response.text(s"Service Healthy")
      }
    }
}
