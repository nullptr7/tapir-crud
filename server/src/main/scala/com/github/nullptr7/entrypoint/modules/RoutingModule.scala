package com.github.nullptr7
package entrypoint
package modules

import org.http4s.HttpApp
import org.typelevel.log4cats.Logger

import cats.effect._

import skunk.Session

import fs2.io.net.Network
import natchez.Trace

import storage._
import protocol.ServiceLogic
import configurations.types._

abstract class RoutingModule[F[_]: Async: std.Console: Trace: Network] {

  implicit val logger: Logger[F]

  final private[modules] def withRoutes(dbConfig: DatabaseConfig): Resource[F, HttpApp[F]] =
    for {
      session     <- DatabaseSession[F].make(dbConfig)
      serverLogic <- initServiceLogic(session).make
      routes      <- Routes.make[F](serverLogic)
    } yield routes

  private[this] def initServiceLogic(session: Session[F]): ServiceLogic[F] = {
    lazy val employeeRepo = EmployeeRepository(session)
    lazy val addressRepo  = AddressRepository(session)
    new ServiceLogic[F](employeeRepo, addressRepo)
  }

}
