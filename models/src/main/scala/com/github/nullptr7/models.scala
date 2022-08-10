package com.github.nullptr7

import java.util.UUID

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec

import monocle.Iso

import optics.IsUUID

package models {

  final case class EmployeeCode(value: UUID) extends AnyVal

  final case class EmployeeId(value: Int) extends AnyVal

  final case class CreateEmployee(
    name:    String,
    age:     Int,
    salary:  Double,
    address: CreateAddress
  )

  final case class Employee(
    id:      EmployeeId,
    code:    EmployeeCode,
    name:    String,
    age:     Int,
    salary:  Double,
    address: Address
  )

  final case class AddressId(value: UUID) extends AnyVal

  final object implicits {

    implicit object EmployeeCodeIso extends IsUUID[EmployeeCode] {

      def _uuid: Iso[UUID, EmployeeCode] =
        Iso[UUID, EmployeeCode](EmployeeCode.apply)(_.value)
    }

    implicit object AddressIdIso extends IsUUID[AddressId] {

      def _uuid: Iso[UUID, AddressId] =
        Iso[UUID, AddressId](AddressId.apply)(_.value)
    }

  }

  final case class CreateAddress(
    street: String,
    city:   String,
    state:  String,
    zip:    String
  )

  final case class Address(
    id:     AddressId,
    street: String,
    city:   String,
    state:  String,
    zip:    String
  )

  final object codecs {

    implicit val employeeCodeCodec:   Codec[EmployeeCode]   = deriveUnwrappedCodec[EmployeeCode]
    implicit val createEmployeeCodec: Codec[CreateEmployee] = deriveCodec[CreateEmployee]
    implicit val employeeIdCodec:     Codec[EmployeeId]     = deriveUnwrappedCodec[EmployeeId]
    implicit val employeeCodec:       Codec[Employee]       = deriveCodec[Employee]
    implicit val addressCodec:        Codec[Address]        = deriveCodec[Address]
    implicit val createAddressCodec:  Codec[CreateAddress]  = deriveCodec[CreateAddress]
    implicit val addressIdCodec:      Codec[AddressId]      = deriveUnwrappedCodec[AddressId]
  }

}
