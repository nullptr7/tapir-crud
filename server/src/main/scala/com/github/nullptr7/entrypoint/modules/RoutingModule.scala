package com.github.nullptr7
package entrypoint
package modules

import org.http4s.HttpApp

import cats.effect._

import sttp.tapir.server.ServerEndpoint

import skunk.Session

import fs2.io.net.Network
import natchez.Trace

import storage._
import protocol.ServiceLogic
import configurations.types._

abstract class RoutingModule[F[_]: Async: std.Console: Trace: Network] {

  final private[modules] def withRoutes(dbConfig: DatabaseConfig): Resource[F, HttpApp[F]] =
    for {
      session     <- DatabaseSession[F].make(dbConfig)
      serverLogic <- Resource.eval(initServiceLogic(session))
      routes      <- Routes.make[F](serverLogic)
    } yield routes

  private[this] def initServiceLogic(session: Session[F]): F[List[ServerEndpoint[Any, F]]] = {
    lazy val employeeRepo = EmployeeRepository(session)
    lazy val addressRepo  = AddressRepository(session)
    lazy val serviceLogic: ServiceLogic[F] =
      new ServiceLogic[F](employeeRepo, addressRepo)
    serviceLogic.make
  }

}
