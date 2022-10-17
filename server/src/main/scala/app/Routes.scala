package app

import algebra.SchemaF
import app.json._
import model.json.JSON
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zhttp.http.{ Http, Request, Response }

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
