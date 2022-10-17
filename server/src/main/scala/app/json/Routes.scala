package app.json

import zhttp.http.{ Http, Request, Response }
import zio.{ ZIO, _ }
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import algebra.SchemaF
import model.json.JSON
import sttp.tapir.swagger.SwaggerUIOptions

object Routes {

  val apiEndpoints = List(
    DownloadRoutes.downloadServerEndpoint,
    UploadRoutes.uploadServerEndpoint,
    ValidateRoutes.validateServerEndpoint
  )

  val swaggerOptions =
    SwaggerUIOptions(List("docs"), "schema-validator.yaml", Nil, useRelativePaths = true)

  val docEndpoints = SwaggerInterpreter()
    .fromServerEndpoints(apiEndpoints, "Schema Validator", "1.0")

  def apply(): Http[SchemaF[JSON], Throwable, Request, Response] =
    ZioHttpInterpreter().toHttp(apiEndpoints ++ docEndpoints)
}
