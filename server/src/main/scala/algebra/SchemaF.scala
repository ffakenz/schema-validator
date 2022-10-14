package algebra

import model.domain
import infra.SchemaRegistry
import zio.{ ZLayer, ZIO }
import zio.{ Tag, Task, RIO }
import service.{ DocumentCleaner, SchemaValidator }

case class SchemaF[A](
    registry: SchemaRegistry[A, Task],
    cleaner: DocumentCleaner[A, Task],
    validator: SchemaValidator[A, Task]
) {

  def download(uri: domain.URI): Task[Option[domain.Schema[A]]] =
    registry.download(uri)

  def upload(schema: domain.Schema[A]): Task[Unit] =
    registry.upload(schema)

  def validate(uri: domain.URI, doc: domain.Document[A]): Task[Either[String, Unit]] =
    for {
      schema   <- registry.download(uri)
      cleanDoc <- cleaner.clean(doc)
      result   <- validator.validate(cleanDoc, schema.get)
    } yield result

}

object SchemaF {

  type Dependencies[A] =
    Any with SchemaRegistry[A, Task] with DocumentCleaner[A, Task] with SchemaValidator[A, Task]

  def layer[A: Tag]: ZLayer[Dependencies[A], Nothing, SchemaF[A]] =
    ZLayer {
      for {
        registry  <- ZIO.service[SchemaRegistry[A, Task]]
        cleaner   <- ZIO.service[DocumentCleaner[A, Task]]
        validator <- ZIO.service[SchemaValidator[A, Task]]
      } yield SchemaF(registry, cleaner, validator)
    }
}
