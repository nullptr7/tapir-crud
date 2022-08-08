package com.github.nullptr7
package entrypoint

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.{IO, IOApp}

import natchez.Trace.Implicits.noop

import modules.BlazeServerModule

object Startup extends BlazeServerModule[IO] with IOApp.Simple {

  override val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    logger.info("Starting Tapir Crud Application...") *>
      server.use(_ => IO.never[Unit])

}
