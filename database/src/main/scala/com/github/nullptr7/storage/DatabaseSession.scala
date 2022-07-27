package com.github.nullptr7
package storage

import cats.effect._
import cats.effect.std.Console

import skunk._

import fs2.io.net.Network
import natchez.Trace

import configurations.types.DatabaseConfig

trait DatabaseSession {

  def sessionR[F[_]: Async: Console: Trace: Network]: Resource[F, Session[F]]
}

object DatabaseSession {

  def create(config: DatabaseConfig) = new DatabaseSession {

    def sessionR[F[_]: Async: Console: Trace: Network]: Resource[F, Session[F]] =
      Session
        .single[F](
          host     = config.host.value,
          port     = config.port.value,
          user     = config.user,
          database = config.database,
          password = Option(config.password.value)
        )

  }

}
