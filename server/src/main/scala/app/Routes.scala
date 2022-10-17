package app

import zhttp.http.{ Http, Request, Response }
import zio.{ ZIO, _ }
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import algebra.SchemaF
import model.json.JSON
import sttp.tapir.swagger.SwaggerUIOptions
import app.HealthRoutes
import sttp.tapir.server.ServerEndpoint
import app.json._

object Routes {

  val apiEndpoints = List(
    HealthRoutes.healthServrEndpoint,
    DownloadRoutes.downloadServerEndpoint,
    UploadRoutes.uploadServerEndpoint,
    ValidateRoutes.validateServerEndpoint
  )

  val swaggerOptions =
    SwaggerUIOptions(List("docs"), "schema-validator.yaml", Nil, useRelativePaths = true)

  val docEndpoints = SwaggerInterpreter(swaggerUIOptions = swaggerOptions)
    .fromServerEndpoints(apiEndpoints, "Schema Validator", "1.0")

  def apply(): Http[SchemaF[JSON], Throwable, Request, Response] =
    ZioHttpInterpreter().toHttp(apiEndpoints ++ docEndpoints)
}
