package com.github.nullptr7

import java.util.UUID

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import monocle.Iso

package models {

  import com.github.nullptr7.optics.IsUUID

  final case class Employee(
    id:      Int,
    name:    String,
    age:     Int,
    salary:  Double,
    address: Address
  )

  final case class AddressId(value: UUID) extends AnyVal

  object implicits {

    implicit object AddressIdIso extends IsUUID[AddressId] {

      def _uuid: Iso[UUID, AddressId] = Iso[UUID, AddressId](AddressId(_))(_.value)
    }

  }

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
    implicit val addressIdCodec:     Codec[AddressId]     = deriveCodec[AddressId]
  }

}
