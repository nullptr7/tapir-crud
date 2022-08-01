package com.github.nullptr7
package entrypoint

import cats.effect.{IO, IOApp}

import natchez.Trace.Implicits.noop

import modules.BlazeServerModule

object Startup extends BlazeServerModule[IO] with IOApp.Simple {

  override def run: IO[Unit] =
    server.use(_ => IO.never[Unit])

}
