package com.github.nullptr7
package exceptions

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class ErrorResponse(statusCode: Int, msg: String)

object ErrorResponse {
  implicit val errorResponseCodec: Codec[ErrorResponse] = deriveCodec[ErrorResponse]
}
