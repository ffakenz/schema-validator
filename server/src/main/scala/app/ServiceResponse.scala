package app

import zio.json._

sealed trait ServiceResposne {
  def action: String
  def id: String
  def status: String
}

object ServiceResposne {

  case class SuccessResponse(
      action: String,
      id: String,
      status: String
  ) extends ServiceResposne

  object SuccessResponse {
    def apply(
        action: String,
        id: String
    ): SuccessResponse = SuccessResponse(action, id, "success")

    implicit val encoder: JsonEncoder[SuccessResponse] =
      DeriveJsonEncoder.gen[SuccessResponse]
  }

  case class ErrorResponse(
      action: String,
      id: String,
      status: String,
      message: String
  ) extends ServiceResposne

  object ErrorResponse {
    def apply(
        action: String,
        id: String,
        message: String
    ): ErrorResponse = ErrorResponse(action, id, "error", message)

    implicit val encoder: JsonEncoder[ErrorResponse] =
      DeriveJsonEncoder.gen[ErrorResponse]
  }

}
