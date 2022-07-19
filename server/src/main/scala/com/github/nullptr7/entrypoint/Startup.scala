package com.github.nullptr7
package entrypoint

import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server

import cats.effect._

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import skunk.Session

import pureconfig.generic.auto.exportReader

import natchez.Trace.Implicits.noop

import storage._
import config.DatabaseConfig
import protocol.ServiceLogic
import config.ConfigLoader

object Startup extends IOApp.Simple {

  private lazy val initializeConfigs: ConfigLoader[IO, DatabaseConfig] =
    ConfigLoader.init[IO, DatabaseConfig](None, "db")

  private def withServer(routes: HttpRoutes[IO]): Resource[IO, Server] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(routes.orNotFound)
      .resource

  private def initServiceLogic(session: Session[IO]): IO[List[ServerEndpoint[Any, IO]]] = {
    val employeeRepo = EmployeeRepository(session)
    val addressRepo  = AddressRepository(session)
    val serviceLogic: ServiceLogic[IO] = new ServiceLogic[IO](employeeRepo, addressRepo)
    serviceLogic.make
  }

  private lazy val withRoutes: Resource[IO, HttpRoutes[IO]] = for {
    dbConfig    <- initializeConfigs.load
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
      routes <- withRoutes
      serve  <- withServer(routes)
    } yield serve

  override def run: IO[Unit] =
    server.use(_ => IO.never[Unit])

}
