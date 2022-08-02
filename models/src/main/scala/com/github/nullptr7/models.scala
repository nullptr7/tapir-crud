package com.github.nullptr7

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

package models {

  import java.util.UUID
  import monocle.Iso

  final case class Employee(
    id:      Int,
    name:    String,
    age:     Int,
    salary:  Double,
    address: Address
  )

  final case class AddressId(value: UUID) extends AnyVal

  object AddressId {

    import optics.IsUUID

    implicit val addressIdToUUID: IsUUID[AddressId] = new IsUUID[AddressId] {

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
