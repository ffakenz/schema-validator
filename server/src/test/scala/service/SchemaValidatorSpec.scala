package service

import zio.test.{ test, _ }

object SchemaValidatorSpec extends ZIOSpecDefault {
  def spec =
    suite("SchemaValidator")(
      impl.JsonSchemaValidatorSuite.jsonSuite
    )
}
