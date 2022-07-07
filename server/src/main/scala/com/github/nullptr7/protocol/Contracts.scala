package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

import models._
import models.codecs._

trait Contracts[F[_]] {

  import AuthMode._

  protected[protocol] lazy val allEmployeesEP: Endpoint[Unit, AuthMode, String, List[Employee], Any] =
    endpoint
      .get
      .in("employees" / "get" / "all")
      .in(header[AuthMode]("X-AuthMode"))
      .out(jsonBody[List[Employee]])
      .errorOut(stringBody)

  protected[protocol] lazy val employeeEP: Endpoint[Unit, (AuthMode, String), String, Option[Employee], Any] =
    endpoint
      .get
      .in("employees" / "get" / "employee")
      .in(header[AuthMode]("X-AuthMode"))
      .in(query[String]("id"))
      .out(jsonBody[Option[Employee]])
      .errorOut(stringBody)

  protected[protocol] val make: List[ServerEndpoint[Any, F]]

}

sealed trait AuthMode
case object Admin    extends AuthMode
case object NonAdmin extends AuthMode

object AuthMode {

  implicit private[protocol] def authModeFromListStringCodec: Codec[List[String], AuthMode, CodecFormat.TextPlain] =
    Codec.listHead[String, AuthMode, CodecFormat.TextPlain]

  implicit private[this] lazy val authModeFromStringCodec: Codec[String, AuthMode, CodecFormat.TextPlain] =
    Codec.string.mapDecode(decode)(encode)

  private[this] def decode(authModeStr: String): DecodeResult[AuthMode] = authModeStr match {
    case "admin"    => DecodeResult.Value(Admin)
    case "nonadmin" => DecodeResult.Value(NonAdmin)
    case _          => DecodeResult.Missing
  }

  private[this] def encode(authMode: AuthMode): String = authMode match {
    case Admin    => "admin"
    case NonAdmin => "nonadmin"
  }

  /*  implicit val authModeDecoder: Decoder[AuthMode] =
     Decoder.decodeString.emap {
       case "admin"    => Right(Admin)
       case "nonadmin" => Right(NonAdmin)
       case _          => Left("Invalid auth mode")
     }*/

}
