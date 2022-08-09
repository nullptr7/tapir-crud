package com.github.nullptr7
package protocol

import org.mockito.MockitoSugar.mock
import org.typelevel.log4cats.Logger

import cats.effect.kernel.Async

import storage._

trait ServiceLogicTestHelper[F[_]] {

  implicit protected def logger: Logger[F]

  implicit protected def async: Async[F]

  protected lazy val serviceLogic: ServiceLogic[F] =
    new ServiceLogic[F](
      mock[EmployeeRepository[F]],
      mock[AddressRepository[F]]
    )

}
