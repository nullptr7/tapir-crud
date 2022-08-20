package com.github.nullptr7
package entrypoint
package modules

import cats.effect.kernel.{Async, Resource}
import cats.implicits._

import pureconfig.generic.auto.exportReader

import configurations.ApplicationResources
import configurations.types.ConfigType.{Client, Server, Postgres}
import configurations.types.{ClientConfig, DatabaseConfig, ServerConfig}
import helpers.ConfigLoader

trait ApplicationResourceModule {

  final def appResources[F[_]: Async]: Resource[F, ApplicationResources] =
    (
      ConfigLoader[F].load[DatabaseConfig, Postgres.type],
      ConfigLoader[F].load[ServerConfig, Server.type],
      ConfigLoader[F].load[ClientConfig, Client.type]
    ).parMapN(ApplicationResources)

}
