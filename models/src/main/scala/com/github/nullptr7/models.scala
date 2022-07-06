package com.github.nullptr7

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec


package models {

  final case class Employee(
    id:      Long,
    name:    String,
    age:     Int,
    salary:  Double,
    address: Address
  )

  final case class Address(
    id:     Long,
    street: String,
    city:   String,
    state:  String,
    zip:    String
  )

  object codecs {

    implicit val employeeCodec: Codec[Employee] = deriveCodec[Employee]
    implicit val addressCodec:  Codec[Address]  = deriveCodec[Address]
  }

}
