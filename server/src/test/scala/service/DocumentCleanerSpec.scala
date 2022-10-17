package service

import zio.test._

object DocumentCleanerSpec extends ZIOSpecDefault {
  def spec =
    suite("DocumentCleaner")(
      json.JsonDocumentCleanerSuite.jsonSuite
    )
}
