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
      id: String
  ) extends ServiceResposne {
    val status: String = "success"
  }

  object SuccessResponse {
    implicit val encoder: JsonEncoder[SuccessResponse] =
      DeriveJsonEncoder.gen[SuccessResponse]
  }

  case class ErrorResponse(
      action: String,
      id: String,
      message: String
  ) extends ServiceResposne {
    val status: String = "error"
  }

  object ErrorResponse {
    implicit val encoder: JsonEncoder[ErrorResponse] =
      DeriveJsonEncoder.gen[ErrorResponse]
  }

}
