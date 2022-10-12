package service

import zio.test.{ test, _ }

object DocumentCleanerSpec extends ZIOSpecDefault {
  def spec =
    suite("DocumentCleaner")(
      impl.JsonDocumentCleanerSuite.jsonSuite
    )
}
