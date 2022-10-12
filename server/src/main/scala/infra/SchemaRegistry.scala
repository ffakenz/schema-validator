package infra

import model.domain._

trait SchemaRegistry[A, F[_]] {

  def download(uri: URI): F[Option[Schema[A]]]

  def upload(schema: Schema[A]): F[Unit]

}
