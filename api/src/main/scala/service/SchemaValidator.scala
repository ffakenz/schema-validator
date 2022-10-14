package service

import model.domain._

trait SchemaValidator[A, F[_]] {

  def validate(
      document: Document[A],
      schema: Schema[A]
  ): F[Either[String, Unit]]

}
