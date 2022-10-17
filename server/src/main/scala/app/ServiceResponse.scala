package app

import zio.json._

object ServiceResponse {

  case class SuccessResponse(
      action: String,
      id: String,
      status: String = "success"
  )

  object SuccessResponse {
    implicit val encoder: JsonEncoder[SuccessResponse] =
      DeriveJsonEncoder.gen[SuccessResponse]

    implicit val decoder: JsonDecoder[SuccessResponse] =
      DeriveJsonDecoder.gen[SuccessResponse]
  }

  sealed trait ErrorResponse {
    def action: String
    def id: String
    def status: String
    def message: String
  }

  object ErrorResponse {
    case class OkErrorResponse(
        action: String,
        id: String,
        status: String = "error",
        message: String
    ) extends ErrorResponse

    object OkErrorResponse {
      implicit val encoder: JsonEncoder[OkErrorResponse] =
        DeriveJsonEncoder.gen[OkErrorResponse]

      implicit val decoder: JsonDecoder[OkErrorResponse] =
        DeriveJsonDecoder.gen[OkErrorResponse]
    }

    case class BadRequestErrorResponse(
        action: String,
        id: String,
        status: String = "error",
        message: String
    ) extends ErrorResponse

    object BadRequestErrorResponse {
      implicit val encoder: JsonEncoder[BadRequestErrorResponse] =
        DeriveJsonEncoder.gen[BadRequestErrorResponse]

      implicit val decoder: JsonDecoder[BadRequestErrorResponse] =
        DeriveJsonDecoder.gen[BadRequestErrorResponse]
    }

    case class InternalErrorResponse(
        action: String,
        id: String,
        status: String = "error",
        message: String
    ) extends ErrorResponse

    object InternalErrorResponse {
      implicit val encoder: JsonEncoder[InternalErrorResponse] =
        DeriveJsonEncoder.gen[InternalErrorResponse]

      implicit val decoder: JsonDecoder[InternalErrorResponse] =
        DeriveJsonDecoder.gen[InternalErrorResponse]
    }

    implicit val encoder: JsonEncoder[ErrorResponse] =
      DeriveJsonEncoder.gen[ErrorResponse]

    implicit val decoder: JsonDecoder[ErrorResponse] =
      DeriveJsonDecoder.gen[ErrorResponse]
  }
}
