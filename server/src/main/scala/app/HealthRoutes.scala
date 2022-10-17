package app

import algebra.SchemaF
import model.json.JSON
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir._
import zhttp.http.{ Http, Request, Response }
import zio.{ ZIO, _ }
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.SwaggerUIOptions

object HealthRoutes {

  private val healthLogic: Unit => Task[Either[Unit, Unit]] = { _ =>
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

  val apiEndpoints = List(healthServrEndpoint)

  val swaggerOptions =
    SwaggerUIOptions(List("docs"), "health.yaml", Nil, useRelativePaths = true)

  val docEndpoints = SwaggerInterpreter(swaggerUIOptions = swaggerOptions)
    .fromServerEndpoints(apiEndpoints, "Health Status", "1.0")

  def apply(): Http[SchemaF[JSON], Throwable, Request, Response] =
    ZioHttpInterpreter().toHttp(apiEndpoints ++ docEndpoints)
}
