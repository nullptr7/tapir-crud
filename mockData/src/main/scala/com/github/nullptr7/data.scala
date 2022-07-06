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

}
