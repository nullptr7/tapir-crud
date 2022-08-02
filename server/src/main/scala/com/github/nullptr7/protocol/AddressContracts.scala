package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import models._
import models.codecs._
import exceptions.ErrorResponse._

trait AddressContracts[F[_]] extends Contracts[F] {

  lazy val addressById =
    base
      .get
      .in("address")
      .in(query[String]("id"))
      .out(jsonBody[Option[Address]])
      .errorOut(jsonBody[ServiceResponseException])

  lazy val addressByPincode =
    base
      .get
      .in("address")
      .in(query[String]("pincode"))
      .out(jsonBody[Option[Address]])
      .errorOut(jsonBody[ServiceResponseException])

  lazy val addAddress =
    base
      .post
      .in("address")
      .in(jsonBody[CreateAddress])
      .out(jsonBody[AddressId])
      .errorOut(jsonBody[ServiceResponseException])

}
