package com.github.nullptr7

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

package models {

  import java.util.UUID

  final case class Employee(
    id:      Int,
    name:    String,
    age:     Int,
    salary:  Double,
    address: Address
  )

  final case class AddressId(value: UUID) extends AnyVal

  final case class CreateAddress(
    street: String,
    city:   String,
    state:  String,
    zip:    String
  )

  final case class Address(
    id:     UUID,
    street: String,
    city:   String,
    state:  String,
    zip:    String
  )

  object codecs {

    implicit val employeeCodec:      Codec[Employee]      = deriveCodec[Employee]
    implicit val addressCodec:       Codec[Address]       = deriveCodec[Address]
    implicit val createAddressCodec: Codec[CreateAddress] = deriveCodec[CreateAddress]
  }

}
