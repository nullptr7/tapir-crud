package com.github.nullptr7
package protocol

import org.mockito.MockitoSugar.mock

import cats.effect.IO

import storage._

trait ServiceLogicTestHelper {

  protected lazy val serviceLogic: ServiceLogic[IO] =
    new ServiceLogic[IO](
      mock[EmployeeRepository[IO]],
      mock[AddressRepository[IO]]
    )

}
