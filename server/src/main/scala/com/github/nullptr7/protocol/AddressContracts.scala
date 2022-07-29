package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import models._
import models.codecs._
import exceptions.ErrorResponse._
import java.util.UUID

trait AddressContracts[F[_]] extends Contracts[F] {

  lazy val addressById =
    base
      .get
      .in("address")
      .in(header[AuthMode]("X-AuthMode"))
      .in(query[String]("id"))
      .out(jsonBody[Option[Address]])
      .errorOut(jsonBody[ServiceResponseException])

  lazy val addressByPincode =
    base
      .get
      .in("address")
      .in(header[AuthMode]("X-AuthMode"))
      .in(query[String]("pincode"))
      .out(jsonBody[Option[Address]])
      .errorOut(jsonBody[ServiceResponseException])

  lazy val addAddress =
    base
      .post
      .in("address")
      .in(jsonBody[CreateAddress])
      .in(header[AuthMode]("X-AuthMode"))
      .out(jsonBody[UUID])
      .errorOut(jsonBody[ServiceResponseException])

}
