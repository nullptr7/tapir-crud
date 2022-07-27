package com.github.nullptr7
package entrypoint

import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server

import cats.effect._
import cats.implicits._

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import skunk.Session

import pureconfig.generic.auto.exportReader

import natchez.Trace.Implicits.noop

import storage._
import protocol.ServiceLogic
import configurations._
import configurations.types._
import helpers.ConfigLoader

object Startup extends IOApp.Simple {

  private lazy val loadResource: Resource[IO, ApplicationResources] =
    (
      ConfigLoader[IO].load[DatabaseConfig](DbDev),
      ConfigLoader[IO].load[BlazeServerConfig](ServerDev)
    ).parMapN(ApplicationResources)

  private def withServer(routes: HttpRoutes[IO], serverConfig: BlazeServerConfig): Resource[IO, Server] =
    BlazeServerBuilder[IO]
      .bindHttp(serverConfig.port.value, serverConfig.host.value)
      .withHttpApp(routes.orNotFound)
      .resource

  private def initServiceLogic(session: Session[IO]): IO[List[ServerEndpoint[Any, IO]]] = {
    lazy val employeeRepo = EmployeeRepository(session)
    lazy val addressRepo  = AddressRepository(session)
    lazy val serviceLogic: ServiceLogic[IO] = new ServiceLogic[IO](employeeRepo, addressRepo)
    serviceLogic.make
  }

  private def withRoutes(dbConfig: DatabaseConfig): Resource[IO, HttpRoutes[IO]] = for {
    session     <- DatabaseSession.create(dbConfig).sessionR[IO]
    serverLogic <- Resource.eval(initServiceLogic(session))
    routes      <- Resource.eval(
                     IO(
                       Http4sServerInterpreter[IO]().toRoutes(serverLogic)
                     )
                   )
  } yield routes

  private lazy val server: Resource[IO, Server] =
    for {
      res    <- loadResource
      routes <- withRoutes(res.databaseConfig)
      serve  <- withServer(routes, res.serverConfig)
    } yield serve

  override def run: IO[Unit] =
    server.use(_ => IO.never[Unit])

}
