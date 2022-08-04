package com.github.nullptr7
package mocks

import java.util.UUID

object data {

  import models._

  val addressId:     AddressId    = AddressId(UUID.randomUUID())
  val employeeId1:   EmployeeId   = EmployeeId(1)
  val employeeId2:   EmployeeId   = EmployeeId(2)
  val employeeCode1: EmployeeCode = EmployeeCode(UUID.randomUUID())
  val employeeCode2: EmployeeCode = EmployeeCode(UUID.randomUUID())

  val allEmployees: List[Employee] = List(
    Employee(
      id      = employeeId1,
      code    = employeeCode1,
      name    = "John",
      age     = 12,
      salary  = 1000,
      address = Address(addressId, "Main Street", "Anytown", "CA", "12345")
    ),
    Employee(
      id      = employeeId2,
      code    = employeeCode2,
      name    = "Doe",
      age     = 17,
      salary  = 1000,
      address = Address(addressId, "Main Street", "Anytown", "CA", "12345")
    )
  )

  val allAddresses: List[Address] = List(
    Address(
      id     = addressId,
      street = "Main Street",
      city   = "Anytown",
      state  = "CA",
      zip    = "12345"
    ),
    Address(
      id     = addressId,
      street = "Main Street",
      city   = "Anytown",
      state  = "CA",
      zip    = "12345"
    )
  )

}
