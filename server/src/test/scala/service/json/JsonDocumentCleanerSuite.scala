package service.json

import zio.ZIO
import zio.test.{ test, _ }
import model.json.{ JsonDocument }
import com.github.fge.jackson.JacksonUtils

object JsonDocumentCleanerSuite {

  def jsonSuite =
    suite("JSON Suite")(
      testCleanDocument,
      testCleanDocumentIdempotent,
      testCleanNestedDocument
    ).provide(JsonDocumentCleaner.live)

  def testCleanDocumentIdempotent =
    test("clean document without nothing to clean") {

      val json = JacksonUtils
        .getReader()
        .readTree("""
          |{
          |  "source": "/home/alice/image.iso",
          |  "chunks": {
          |    "size": 1024
          |  }
          |}
        """.stripMargin)
      val jsonDocument = JsonDocument(json)

      ZIO.serviceWithZIO[JsonDocumentCleaner] { cleaner =>
        for {
          result <- cleaner.clean(jsonDocument)
        } yield assertTrue(result == jsonDocument)
      }
    }

  def testCleanDocument =
    test("clean document from null values") {

      val json = JacksonUtils
        .getReader()
        .readTree("""
          |{
          |  "source": null,
          |  "chunks": {
          |    "size": 1024
          |  }
          |}
        """.stripMargin)
      val jsonDocument = JsonDocument(json)

      val expectedJson = JacksonUtils
        .getReader()
        .readTree("""
          |{
          |  "chunks": {
          |    "size": 1024
          |  }
          |}
        """.stripMargin)
      val expectedJsonDocument = JsonDocument(expectedJson)

      ZIO.serviceWithZIO[JsonDocumentCleaner] { cleaner =>
        for {
          result <- cleaner.clean(jsonDocument)
        } yield assertTrue(result == expectedJsonDocument)
      }
    }

  def testCleanNestedDocument =
    test("clean nested document from null values") {

      val json = JacksonUtils
        .getReader()
        .readTree("""
          |{
          |  "a": "value",
          |  "b": null,
          |  "c": {
          |    "d": "value"
          |  },
          |  "e": {
          |    "f": {
          |      "g": "value"
          |    }
          |  },
          |  "h": {
          |    "i": null
          |  },
          |  "j": {
          |    "k": {
          |      "l": null
          |    }
          |  },
          |  "m": [
          |    { "n": "value" }
          |  ],
          |  "o": [
          |    { "p": null }
          |  ],
          |  "o": [
          |    [{ "p": null }]
          |  ]
          |}
        """.stripMargin)
      val jsonDocument = JsonDocument(json)

      val expectedJson = JacksonUtils
        .getReader()
        .readTree("""
          |{
          |  "a": "value",
          |  "c": {
          |    "d": "value"
          |  },
          |  "e": {
          |    "f": {
          |      "g": "value"
          |    }
          |  },
          |  "m": [
          |    { "n": "value" }
          |  ],
          |  "o": [
          |
          |  ],
          |  "o": [
          |   []
          |  ]
          |}""".stripMargin)
      val expectedJsonDocument = JsonDocument(expectedJson)

      ZIO.serviceWithZIO[JsonDocumentCleaner] { cleaner =>
        for {
          result <- cleaner.clean(jsonDocument)
        } yield assertTrue(result == expectedJsonDocument)
      }
    }
}
