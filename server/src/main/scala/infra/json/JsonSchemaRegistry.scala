package infra.json

import model.domain.{ Schema, URI }
import model.json.JSON
import zio.ZIO
import zio.ZLayer
import JsonSchemaRegistry.{ Registry, Z }
import infra.SchemaRegistry
import zio.Ref

// @TODO use zio-json
case class JsonSchemaRegistry() extends SchemaRegistry[JSON, Z] {

  def download(uri: URI): Z[Option[Schema[JSON]]] =
    ZIO.serviceWithZIO[Registry] { ref =>
      ref.get.map { registry =>
        registry.get(uri)
      }
    }

  def upload(schema: Schema[JSON]): Z[Unit] =
    ZIO.serviceWithZIO[Registry] { ref =>
      ref.modify { registry =>
        ((), registry.updated(schema.uri, schema))
      }
    }
}

object JsonSchemaRegistry {
  type Registry = Ref[Map[URI, Schema[JSON]]]

  type Z[A] = ZIO[Registry, Throwable, A]

  val layer: ZLayer[Any, Nothing, JsonSchemaRegistry] =
    ZLayer {
      ZIO.succeed(JsonSchemaRegistry())
    }
}
