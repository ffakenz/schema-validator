package app

import zio.test._

object RoutesSpec extends ZIOSpecDefault {
  def spec =
    suite("Routes")(
      json.JsonRoutesSuite.jsonSuite
    )
}
