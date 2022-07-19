package com.github.nullptr7
package protocol

import sttp.tapir.{DecodeResult, _}

sealed trait AuthMode

case object Admin       extends AuthMode
case object NonAdmin    extends AuthMode
case object InvalidMode extends AuthMode

object AuthMode {

  implicit private[protocol] def authModeFromListStringCodec: Codec[List[String], AuthMode, CodecFormat.TextPlain] =
    Codec.listHead[String, AuthMode, CodecFormat.TextPlain]

  implicit private[this] lazy val authModeFromStringCodec: Codec[String, AuthMode, CodecFormat.TextPlain] =
    Codec.string.mapDecode(decode)(encode)

  private[this] def decode(authModeStr: String): DecodeResult[AuthMode] = authModeStr match {
    case "admin"    => DecodeResult.Value(Admin)
    case "nonadmin" => DecodeResult.Value(NonAdmin)
    case _          => DecodeResult.Value(InvalidMode)
  }

  private[this] def encode(authMode: AuthMode): String = authMode match {
    case Admin       => "admin"
    case NonAdmin    => "nonadmin"
    case InvalidMode => "invalid"
  }

  /*  implicit val authModeDecoder: Decoder[AuthMode] =
     Decoder.decodeString.emap {
       case "admin"    => Right(Admin)
       case "nonadmin" => Right(NonAdmin)
       case _          => Left("Invalid auth mode")
     }*/

}
