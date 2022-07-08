package com.github.nullptr7

package object data {

  import models._

  val allEmployees: List[Employee] = List(
    Employee(
      id      = 1,
      name    = "John",
      age     = 12,
      salary  = 1000,
      address = Address(123, "Main Street", "Anytown", "CA", "12345")
    ),
    Employee(
      id      = 2,
      name    = "Doe",
      age     = 17,
      salary  = 1000,
      address = Address(123, "Main Street", "Anytown", "CA", "12345")
    )
  )

  val allAddresses: List[Address] = List(
    Address(
      id     = 123,
      street = "Main Street",
      city   = "Anytown",
      state  = "CA",
      zip    = "12345"
    ),
    Address(
      id     = 456,
      street = "Main Street",
      city   = "Anytown",
      state  = "CA",
      zip    = "12345"
    )
  )

}
