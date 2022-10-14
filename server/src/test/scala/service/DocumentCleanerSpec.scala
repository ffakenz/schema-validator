package service

import zio.test.{ test, _ }

object DocumentCleanerSpec extends ZIOSpecDefault {
  def spec =
    suite("DocumentCleaner")(
      json.JsonDocumentCleanerSuite.jsonSuite
    )
}
