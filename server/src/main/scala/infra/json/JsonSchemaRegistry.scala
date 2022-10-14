package infra.json

import infra.SchemaRegistry
import infra.json.JsonSchemaRegistry.Registry
import model.domain.{ Schema, URI }
import model.json.JSON
import zio.{ Ref, Task, ZLayer }

case class JsonSchemaRegistry(
    ref: Registry
) extends SchemaRegistry[JSON, Task] {

  def download(uri: URI): Task[Option[Schema[JSON]]] =
    ref.get.map { registry =>
      registry.get(uri)
    }

  def upload(schema: Schema[JSON]): Task[Unit] =
    ref.modify { registry =>
      ((), registry.updated(schema.uri, schema))
    }
}

object JsonSchemaRegistry {
  type Registry = Ref[Map[URI, Schema[JSON]]]

  val live: ZLayer[Any, Nothing, JsonSchemaRegistry] =
    ZLayer {
      for {
        ref <- Ref.make(Map.empty[URI, Schema[JSON]])
      } yield JsonSchemaRegistry(ref)
    }
}
