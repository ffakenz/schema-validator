import zio._
import zio.test.{ test, _ }
import zio.test.Assertion._

import zio.test._

object ServiceSpec extends ZIOSpecDefault {
  def spec =
    suite("ApplicationSpec")(
      test("assert true") {
        for {
          value <- ZIO.succeed(true)
        } yield assertTrue(value)
      }
    )
}
