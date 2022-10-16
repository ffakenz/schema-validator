package app

import zio.json._

sealed trait ServiceResponse {
  def action: String
  def id: String
  def status: String
}

object ServiceResponse {

  case class SuccessResponse(
      action: String,
      id: String,
      status: String
  ) extends ServiceResponse

  object SuccessResponse {
    def apply(
        action: String,
        id: String
    ): SuccessResponse = SuccessResponse(action, id, "success")

    implicit val encoder: JsonEncoder[SuccessResponse] =
      DeriveJsonEncoder.gen[SuccessResponse]

    implicit val decoder: JsonDecoder[SuccessResponse] =
      DeriveJsonDecoder.gen[SuccessResponse]
  }

  case class ErrorResponse(
      action: String,
      id: String,
      status: String,
      message: String
  ) extends ServiceResponse

  object ErrorResponse {
    def apply(
        action: String,
        id: String,
        message: String
    ): ErrorResponse = ErrorResponse(action, id, "error", message)

    implicit val encoder: JsonEncoder[ErrorResponse] =
      DeriveJsonEncoder.gen[ErrorResponse]

    implicit val decoder: JsonDecoder[ErrorResponse] =
      DeriveJsonDecoder.gen[ErrorResponse]
  }

  implicit val encoder: JsonEncoder[ServiceResponse] =
    DeriveJsonEncoder.gen[ServiceResponse]
}
