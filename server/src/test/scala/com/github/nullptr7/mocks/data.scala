package com.github.nullptr7
package mocks

import java.util.UUID

object data {

  import models._

  val uuid: UUID = UUID.randomUUID()

  val allEmployees: List[Employee] = List(
    Employee(
      id      = 1,
      name    = "John",
      age     = 12,
      salary  = 1000,
      address = Address(uuid, "Main Street", "Anytown", "CA", "12345")
    ),
    Employee(
      id      = 2,
      name    = "Doe",
      age     = 17,
      salary  = 1000,
      address = Address(uuid, "Main Street", "Anytown", "CA", "12345")
    )
  )

  val allAddresses: List[Address] = List(
    Address(
      id     = uuid,
      street = "Main Street",
      city   = "Anytown",
      state  = "CA",
      zip    = "12345"
    ),
    Address(
      id     = uuid,
      street = "Main Street",
      city   = "Anytown",
      state  = "CA",
      zip    = "12345"
    )
  )

}