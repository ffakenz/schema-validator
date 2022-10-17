package app

import algebra.SchemaF
import model.json.JSON
import sttp.tapir.ztapir._
import zio.{ RIO, ZIO }

object HealthRoutes {

  private val healthLogic: Unit => RIO[SchemaF[JSON], Either[Unit, Unit]] = { _ =>
    ZIO.unit.map(Right.apply)
  }

  private val healthEndpoint =
    endpoint
      .name("Service health status")
      .get
      .in("health")
      .in(emptyInput)
      .out(emptyOutput)

  val healthServrEndpoint =
    healthEndpoint
      .serverLogic(healthLogic)
}
