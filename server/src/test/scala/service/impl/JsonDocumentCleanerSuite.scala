package service.impl

import zio.ZIO
import zio.test.{ test, _ }
import model.json.{ JsonDocument }
import com.github.fge.jackson.JacksonUtils

object JsonDocumentCleanerSuite {

  def jsonSuite =
    suite("JSON Suite")(
      testCleanDocumentIdempotent
    ).provide(JsonDocumentCleaner.layer)

  def testCleanDocumentIdempotent =
    test("clean document without nothing to clean") {

      val json         = JacksonUtils.nodeFactory().objectNode()
      val jsonDocument = JsonDocument(json)

      ZIO.serviceWithZIO[JsonDocumentCleaner] { cleaner =>
        for {
          result <- cleaner.clean(jsonDocument)
        } yield assertTrue(result == jsonDocument)
      }
    }
}
