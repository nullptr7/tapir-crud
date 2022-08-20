package com.github.nullptr7

package models {

  import java.util.UUID

  import io.circe._
  import io.circe.generic.extras.semiauto.{deriveEnumerationCodec, deriveUnwrappedCodec}
  import io.circe.generic.semiauto.deriveCodec

  import monocle.Iso

  import optics.IsUUID

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

  sealed trait Shift extends Enumeration

  case object DAY extends Shift

  case object AFTERNOON extends Shift

  case object EVENING extends Shift

  case object NIGHT extends Shift

  case object NA extends Shift

  final case class TransportRequest(code: EmployeeCode)

  final case class TransportResponse(employeeCode: EmployeeCode, routes: Int, numberOfNoShows: Int, shift: Shift)

  object TransportResponse {

    def apply(employeeCode: EmployeeCode): TransportResponse = TransportResponse(
      employeeCode    = employeeCode,
      routes          = 0,
      numberOfNoShows = 0,
      shift           = NA
    )

  }

  final case class EmployeeWithTransport(
    id:               EmployeeId,
    code:             EmployeeCode,
    name:             String,
    age:              Int,
    salary:           Double,
    address:          Address,
    transportDetails: TransportResponse
  )

  object EmployeeWithTransport {

    def apply(employee: Employee, transportDetails: TransportResponse): EmployeeWithTransport =
      new EmployeeWithTransport(
        employee.id,
        employee.code,
        employee.name,
        employee.age,
        employee.salary,
        employee.address,
        transportDetails
      )

  }

  final case class AddressId(value: UUID) extends AnyVal

  object implicits {

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

  object codecs {

    implicit lazy val employeeCodeCodec:          Codec[EmployeeCode]          = deriveUnwrappedCodec[EmployeeCode]
    implicit lazy val employeeIdCodec:            Codec[EmployeeId]            = deriveUnwrappedCodec[EmployeeId]
    implicit lazy val createEmployeeCodec:        Codec[CreateEmployee]        = deriveCodec[CreateEmployee]
    implicit lazy val employeeCodec:              Codec[Employee]              = deriveCodec[Employee]
    implicit lazy val addressCodec:               Codec[Address]               = deriveCodec[Address]
    implicit lazy val createAddressCodec:         Codec[CreateAddress]         = deriveCodec[CreateAddress]
    implicit lazy val addressIdCodec:             Codec[AddressId]             = deriveUnwrappedCodec[AddressId]
    implicit lazy val shiftCodec:                 Codec[Shift]                 = deriveEnumerationCodec[Shift]
    implicit lazy val transportResponseCodec:     Codec[TransportResponse]     = deriveCodec[TransportResponse]
    implicit lazy val transportRequestCodec:      Codec[TransportRequest]      = deriveCodec[TransportRequest]
    implicit lazy val employeeWithTransportCodec: Codec[EmployeeWithTransport] = deriveCodec[EmployeeWithTransport]
  }

}
