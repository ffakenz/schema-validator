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
import JacksonValidorClient._
import scala.util.Try

import scala.jdk.CollectionConverters._
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jackson.JsonNodeReader
import com.github.fge.jackson.JacksonUtils

case class JacksonValidorClient() {

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
          .map {
            case ex: ProcessingException => handleException(ex)
            case throwable               => throwable.getMessage()
          }
          .flatMap { report =>
            if (report.isSuccess())
              Right(())
            else
              Left(
                report.asScala
                  .map { msg =>
                    handleException(msg.asException())
                  }
                  .mkString(",")
              )
          }
      )
    }

  private def handleException(ex: ProcessingException): String = {
    handleSyntaxError(ex)
      .orElse(handleFatalError(ex))
      .getOrElse(ex.getMessage())
  }

  private def handleFatalError(ex: ProcessingException): Option[String] = {
    /* the error message is a multi-line text
    where each line has a key and a value.
    here we are only inte cleaning the error message to extract the valid json. */
    ex
      .getMessage()
      .split("\n")
      .find(_.startsWith("fatal: "))
      .map(_.replace("fatal: ", ""))
  }

  private def handleSyntaxError(ex: ProcessingException): Option[String] = Try {
    /* the error message is a multi-line text
    that contiains a valid json between a header and a footer.
    here we are cleaning the error message to extract the valid json. */
    val str = ex
      .getMessage()
      .split("\n")
      .drop(2)      // drop header
      .dropRight(1) // drop footer
      .mkString

    JacksonUtils
      .getReader()
      .readTree(str)
      .asScala
      .map(json => json.at("/message").asText())
      .mkString

  }.toOption
}

object JacksonValidorClient {

  type Z[A] = ZIO[JsonValidator, Throwable, A]

  val jsonValidator: ZLayer[Any, Throwable, JsonValidator] = ZLayer {
    ZIO.fromTry(
      Try {
        JsonSchemaFactory.byDefault().getValidator()
      }
    )
  }

  val jacksonClient: ZLayer[JsonValidator, Nothing, JacksonValidorClient] =
    ZLayer {
      ZIO.succeed(JacksonValidorClient())
    }
}
