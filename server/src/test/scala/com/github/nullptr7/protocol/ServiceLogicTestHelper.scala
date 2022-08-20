package com.github.nullptr7
package protocol

import org.mockito.MockitoSugar.mock
import org.typelevel.log4cats.Logger

import cats.effect.kernel.Async

import client.ApiClients
import entrypoint.modules.{RepositoryModule, ServiceLogicModule}

trait ServiceLogicTestHelper[F[_]] {

  implicit protected def logger: Logger[F]

  implicit protected def async: Async[F]

  protected lazy val serverLogicModule: ServiceLogicModule[F] =
    new ServiceLogicModule[F](mock[RepositoryModule[F]], mock[ApiClients[F]])

}
