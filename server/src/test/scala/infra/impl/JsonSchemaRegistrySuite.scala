package infra.impl

import zio.{ ZIO, Ref }
import zio.test.{ test, _ }
import model.json._
import zio.ZLayer
import zio.ZEnvironment
import model.domain._
import zio.UIO
import com.github.fge.jackson.JacksonUtils

object JsonSchemaRegistrySuite {

  val registry: ZLayer[Any, Nothing, JsonSchemaRegistry.Registry] =
    ZLayer(Ref.make(Map.empty))

  def jsonSuite =
    suite("JSON Suite")(
      testNotFound,
      testUploadSchema,
      testShouldOverrideExisting
    ).provide(registry, JsonSchemaRegistry.layer)

  def testNotFound =
    test("download none existing schema id") {
      ZIO.serviceWithZIO[JsonSchemaRegistry] { registry =>
        for {
          maybeSchema <- registry.download(SchemaId("schema-1"))
        } yield assertTrue(maybeSchema == Option.empty[JsonSchema])
      }
    }

  def testUploadSchema =
    test("upload and downlaod schema") {
      val spec = JacksonUtils.getReader().readTree("{}")
      val expectedSchema =
        JsonSchema(uri = SchemaId("schema-1"), spec = spec)

      ZIO.serviceWithZIO[JsonSchemaRegistry] { registry =>
        for {
          _           <- registry.upload(expectedSchema)
          maybeSchema <- registry.download(expectedSchema.uri)
        } yield assertTrue(maybeSchema == Some(expectedSchema))
      }
    }

  def testShouldOverrideExisting =
    test("upload and override existing schema") {
      val spec = JacksonUtils.getReader().readTree("{}")
      val original =
        JsonSchema(uri = SchemaId("schema-1"), spec = spec)

      val newSpec = JacksonUtils.nodeFactory().arrayNode()
      val expected =
        original.copy(spec = newSpec)

      ZIO.serviceWithZIO[JsonSchemaRegistry] { registry =>
        for {
          _           <- registry.upload(original)
          _           <- registry.upload(expected)
          maybeSchema <- registry.download(original.uri)
        } yield assertTrue(maybeSchema == Some(expected))
      }
    }
}
