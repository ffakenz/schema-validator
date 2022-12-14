package app.json

import app.Routes
import app.ServiceResponse.ErrorResponse._
import app.ServiceResponse._
import com.github.fge.jackson.JacksonUtils
import utils.FileUtils.acquire
import zhttp.http._
import zio.Scope
import zio.json._
import zio.test.{ test, _ }

object JsonRoutesSuite {

  val routes = Routes()

  def jsonSuite =
    suite("JSON Suite")(
      testHealthStatus,
      testDownloadNotFound,
      testUploadInvalidJson,
      testUploadSuccess,
      testValidateFail,
      testValidateFailInvalodJSON,
      testValidateFailNotFound,
      testValidateSuccess
    ).provide(
      infra.json.Layers.schemaRegistry,
      infra.json.Layers.schemaValidator,
      infra.json.Layers.documentCleaner,
      infra.json.Layers.jsonValidator,
      algebra.SchemaF.layer[model.json.JSON],
      Scope.default
    )

  def testHealthStatus =
    test("[health] status ok") {
      val req = Request(
        method = Method.GET,
        url = URL(!! / "health")
      )
      for {
        response <- routes(req)
        body     <- response.bodyAsString
      } yield assertTrue(response.status == Status.Ok) &&
        assertTrue(body.isEmpty)
    }

  def testDownloadNotFound =
    test("[downoad] fail because of: Schema not found") {
      val req = Request(
        method = Method.GET,
        url = URL(!! / "schema" / "1")
      )
      for {
        response <- routes(req)
        body     <- response.bodyAsString
      } yield assertTrue(response.status == Status.NotFound) &&
        assertTrue(body.isEmpty)
    }

  def testUploadInvalidJson =
    test("[upload] fail because of: Invalid JSON") {
      val req = Request(
        method = Method.POST,
        url = URL(!! / "schema" / "1")
      )
      for {
        response <- routes(req)
        body     <- response.bodyAsString
      } yield assertTrue(response.status == Status.BadRequest) &&
        assertTrue(
          body.fromJson[BadRequestErrorResponse] ==
            Right(BadRequestErrorResponse("upload", "1", message = "Invalid JSON"))
        )
    }

  def testUploadSuccess =
    test("[upload] success") {
      def uploadReq(bodyStr: String) = Request(
        method = Method.POST,
        url = URL(!! / "schema" / "1"),
        data = HttpData.fromString(bodyStr)
      )
      val downloadReq = Request(
        method = Method.GET,
        url = URL(!! / "schema" / "1")
      )
      for {
        file <- acquire("server/src/test/resources/config-schema.json")
        configStr = file.getLines.mkString
        schema    = JacksonUtils.getReader.readTree(configStr).toPrettyString
        uploadResponse       <- routes(uploadReq(schema))
        uploadResponseBody   <- uploadResponse.bodyAsString
        downloadResponse     <- routes(downloadReq)
        downloadResponseBody <- downloadResponse.bodyAsString
      } yield assertTrue(uploadResponse.status == Status.Created) &&
        assertTrue(
          uploadResponseBody.fromJson[SuccessResponse] ==
            Right(SuccessResponse("upload", "1"))
        ) && assertTrue(downloadResponse.status == Status.Ok) &&
        assertTrue(
          downloadResponseBody == schema
        )
    }

  def testValidateFailInvalodJSON =
    test("[validate] fail because of: Invalid JSON") {
      val validateReq = Request(
        method = Method.POST,
        url = URL(!! / "validate" / "1")
      )
      for {
        response <- routes(validateReq)
        body     <- response.bodyAsString
      } yield assertTrue(response.status == Status.InternalServerError) && // @TODO bad request
        assertTrue(
          body.fromJson[BadRequestErrorResponse] ==
            Right(BadRequestErrorResponse("validate", "1", message = "Invalid JSON"))
        )
    }

  def testValidateFailNotFound =
    test("[validate] fail because of: Schema not found") {
      val validateReq = Request(
        method = Method.POST,
        url = URL(!! / "validate" / "1"),
        data = HttpData.fromString("{}")
      )
      for {
        response <- routes(validateReq)
        body     <- response.bodyAsString
      } yield assertTrue(response.status == Status.Ok) &&
        assertTrue(
          body.fromJson[OkErrorResponse] ==
            Right(OkErrorResponse("validate", "1", message = "Schema not found"))
        )
    }

  def testValidateFail =
    test("[validate] fail because of: Document does not match Schema") {
      def uploadReq(bodyStr: String) = Request(
        method = Method.POST,
        url = URL(!! / "schema" / "1"),
        data = HttpData.fromString(bodyStr)
      )
      val validateReq = Request(
        method = Method.POST,
        url = URL(!! / "validate" / "1"),
        data = HttpData.fromString("{}")
      )
      for {
        file <- acquire("server/src/test/resources/config-schema.json")
        configStr = file.getLines.mkString
        schema    = JacksonUtils.getReader.readTree(configStr).toPrettyString
        -                    <- routes(uploadReq(schema))
        validateResponse     <- routes(validateReq)
        validateResponseBody <- validateResponse.bodyAsString
      } yield assertTrue(validateResponse.status == Status.Ok) &&
        assertTrue(
          validateResponseBody.fromJson[OkErrorResponse] ==
            Right(
              OkErrorResponse(
                "validate",
                "1",
                message = """[ [0]: object has missing required properties (["destination","source"]) ]"""
              )
            )
        )
    }

  def testValidateSuccess =
    test("[validate] success") {
      def uploadReq(bodyStr: String) = Request(
        method = Method.POST,
        url = URL(!! / "schema" / "1"),
        data = HttpData.fromString(bodyStr)
      )
      def validateReq(bodyStr: String) = Request(
        method = Method.POST,
        url = URL(!! / "validate" / "1"),
        data = HttpData.fromString(bodyStr)
      )
      for {
        file1 <- acquire("server/src/test/resources/config-schema.json")
        configStr1 = file1.getLines.mkString
        schema     = JacksonUtils.getReader.readTree(configStr1).toPrettyString
        -     <- routes(uploadReq(schema))
        file2 <- acquire("server/src/test/resources/config.json")
        configStr2 = file2.getLines.mkString
        doc        = JacksonUtils.getReader.readTree(configStr2).toPrettyString
        validateResponse     <- routes(validateReq(doc))
        validateResponseBody <- validateResponse.bodyAsString
      } yield assertTrue(validateResponse.status == Status.Ok) &&
        assertTrue(
          validateResponseBody.fromJson[SuccessResponse] ==
            Right(SuccessResponse("validate", "1"))
        )
    }
}
