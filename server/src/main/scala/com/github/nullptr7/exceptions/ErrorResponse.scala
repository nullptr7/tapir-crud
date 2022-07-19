package com.github.nullptr7
package exceptions

import sttp.tapir._
import sttp.tapir.generic.auto._

import io.circe.Decoder.Result
import io.circe.generic.semiauto.deriveCodec
import io.circe.{HCursor, Json, JsonObject}

case class ErrorResponse(serviceException: String) extends Exception

object ErrorResponse {

  import io.circe.Codec
  sealed abstract class ServiceResponseException(val code: Int, val msg: String) extends Exception(msg)

  final object InvalidAuthException      extends ServiceResponseException(400, "Invalid Authentication Provided")
  final object UnauthorizedAuthException extends ServiceResponseException(401, "Unauthorized!")
  final object UnknownException          extends ServiceResponseException(500, "Internal Business Error")
  final case class GenericException(override val msg: String) extends ServiceResponseException(500, msg)

  implicit val serviceRespExCodec: Codec.AsObject[ServiceResponseException] = new Codec.AsObject[ServiceResponseException] {

    override def apply(c: HCursor): Result[ServiceResponseException] =
      c.downField("code").as[Int].flatMap {
        case 400 => Right(InvalidAuthException)
        case 401 => Right(UnauthorizedAuthException)
        case _   => Right(c.downField("message").as[String].map(GenericException).getOrElse(UnknownException))
      }

    override def encodeObject(errorResp: ServiceResponseException): JsonObject =
      JsonObject(
        ("code", Json.fromInt(errorResp.code)),
        ("message", Json.fromString(errorResp.msg))
      )

  }

  implicit val serviceResponseExceptionSchema: Schema[ServiceResponseException] = Schema.derivedSchema[ServiceResponseException]

  implicit val errorResponseCodec: Codec[ErrorResponse] = deriveCodec[ErrorResponse]
}
