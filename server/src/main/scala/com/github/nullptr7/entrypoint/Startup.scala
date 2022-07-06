package com.github.nullptr7
package entrypoint

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Startup extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    import protocol.ServiceLogic
    val allRoutes = Http4sServerInterpreter[IO]().toRoutes(new ServiceLogic[IO].make)

    BlazeServerBuilder[IO]
      .bindHttp(8080)
      .withHttpApp(allRoutes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
