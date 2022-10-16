package app

import model.json.JSON
import zio.ZIO
import sttp.tapir.ztapir._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zhttp.http.{ Http, Request, Response }
import zio._
import algebra.SchemaF

object HealthRoutes {

  val healthLogic: Unit => Task[Either[Unit, Unit]] = { _ =>
    ZIO.unit.map(Right.apply)
  }

  val healthSchema = ZioHttpInterpreter().toHttp(
    endpoint
      .name("Service health status")
      .get
      .in("health")
      .in(emptyInput)
      .out(emptyOutput)
      .serverLogic(healthLogic)
  )

  def apply(): Http[SchemaF[JSON], Throwable, Request, Response] =
    healthSchema
}
