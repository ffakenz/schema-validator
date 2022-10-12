package infra.impl

import zio.ZIO
import zio.ZLayer
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.exceptions.ProcessingException
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.main.JsonValidator
import model.json._
import JsonValidorClient._
import scala.util.Try

import scala.jdk.CollectionConverters._

case class JsonValidorClient() {

  def validate(
      schema: JsonNode,
      instance: JsonNode,
      deepCheck: Boolean = true
  ): Z[Either[String, Unit]] =
    ZIO.serviceWithZIO[JsonValidator] { validator =>
      ZIO.succeed(
        Try {
          val resultReport = validator.validate(schema, instance, deepCheck)
          resultReport
        }.toEither.left
          .map(throwable => throwable.getMessage())
          .flatMap { report =>
            if (report.isSuccess())
              Right(())
            else
              Left(
                report.asScala
                  .map(msg => msg.asException().getMessage())
                  .toList
                  .mkString(",")
              )
          }
      )
    }
}

object JsonValidorClient {

  type Z[A] = ZIO[JsonValidator, Throwable, A]

  val jsonValidator: ZLayer[Any, Throwable, JsonValidator] = ZLayer {
    ZIO.fromTry(
      Try {
        JsonSchemaFactory.byDefault().getValidator()
      }
    )
  }

  val layer: ZLayer[JsonValidator, Nothing, JsonValidorClient] =
    ZLayer {
      ZIO.succeed(JsonValidorClient())
    }
}
