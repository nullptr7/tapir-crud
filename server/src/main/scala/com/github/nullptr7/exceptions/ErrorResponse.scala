package com.github.nullptr7
package exceptions

import io.circe.Decoder.Result
import io.circe.HCursor
import io.circe.Json
import io.circe.JsonObject
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir._
import sttp.tapir.generic.auto._

case class ErrorResponse(serviceException: String) extends Exception

object ErrorResponse {

  import io.circe.Codec
  sealed abstract class ServiceResponseException(val code: Int, val msg: String) extends Exception(msg)

  final object InvalidAuthException      extends ServiceResponseException(400, "Invalid Authentication Provided")
  final object UnauthorizedAuthException extends ServiceResponseException(401, "Unauthorized!")

  final object GenericException extends ServiceResponseException(500, "Internal Server Error")

  implicit val serviceRespExCodec: Codec.AsObject[ServiceResponseException] = new Codec.AsObject[ServiceResponseException] {

    override def apply(c: HCursor): Result[ServiceResponseException] =
      c.downField("code").as[Int].flatMap {
        case 400 => Right(InvalidAuthException)
        case 401 => Right(UnauthorizedAuthException)
        case _   => Right(GenericException)
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
