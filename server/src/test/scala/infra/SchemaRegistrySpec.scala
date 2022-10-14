package infra

import zio.test.{ test, _ }

object SchemaRegistrySpec extends ZIOSpecDefault {
  def spec =
    suite("SchemaRegistry")(
      json.JsonSchemaRegistrySuite.jsonSuite
    )
}
