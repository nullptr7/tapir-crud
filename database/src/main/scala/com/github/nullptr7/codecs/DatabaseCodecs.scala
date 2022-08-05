package com.github.nullptr7
package codecs

import skunk._
import skunk.codec.all._

import models.{Address, AddressId, Employee, EmployeeCode, EmployeeId}

object DatabaseCodecs {

  // val addressIdEncoder: Encoder[AddressId] = uuid.values.gcontramap[AddressId]

  // val addressIdDecoder: Decoder[AddressId] = uuid.asDecoder.map(AddressId)

  lazy val addressIdCodec: Codec[AddressId] = uuid.imap[AddressId](AddressId)(_.value)

  lazy val dbToAddressDecoder: Decoder[Address] =
    (addressIdCodec.asDecoder ~ text ~ text ~ text ~ text).map { case id ~ street ~ city ~ state ~ zip =>
      Address(id, street, city, state, zip)
    }

  lazy val employeeCodeCodec: Codec[EmployeeCode] = uuid.imap[EmployeeCode](EmployeeCode)(_.value)

  lazy val employeeIdCodec: Codec[EmployeeId] = int4.imap(EmployeeId)(_.value)

  lazy val dbToEmployeeDecoder: Decoder[Employee] =
    (employeeIdCodec.asDecoder ~ employeeCodeCodec ~ text ~ int4 ~ float8 ~ dbToAddressDecoder).map {
      case id ~ code ~ name ~ age ~ salary ~ address =>
        Employee(id, code, name, age, salary, address)
    }

}
