package com.github.nullptr7
package helper

import cats.effect._
import cats.effect.std.Console

import skunk.Session

import fs2.io.net.Network
import natchez.Trace.Implicits.noop

trait PostgresSessionHelper[F[_]] {

  implicit val concurrent: Concurrent[F]
  implicit val network:    Network[F]
  implicit val console:    Console[F]

  final def sessionR: Resource[F, Session[F]] =
    Session
      .single[F](
        host     = "localhost",
        port     = 5432,
        user     = "nullptr7",
        database = "postgres",
        password = Option("nullptr@7")
      )

}
