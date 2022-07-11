package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import models._
import models.codecs._
import exceptions.ErrorResponse._

trait AddressContracts[F[_]] extends Contracts[F] {

  final private[this] def addressBy =
    base
      .get
      .in("address")
      .in(header[AuthMode]("X-AuthMode"))
      .out(jsonBody[Option[Address]])
      .errorOut(jsonBody[ServiceResponseException])

  lazy val addressById = addressBy.in(query[String]("id"))

  lazy val addressByPincode = addressBy.in(query[String]("pincode"))

}
