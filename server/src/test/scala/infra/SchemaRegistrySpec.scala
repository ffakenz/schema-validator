package infra

import zio.test._

object SchemaRegistrySpec extends ZIOSpecDefault {
  def spec =
    suite("SchemaRegistry")(
      json.JsonSchemaRegistrySuite.jsonSuite
    )
}
