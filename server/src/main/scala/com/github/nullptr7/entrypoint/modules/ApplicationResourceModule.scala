package com.github.nullptr7
package entrypoint
package modules

import cats.effect.kernel.{Async, Resource}
import cats.implicits._

import pureconfig.generic.auto.exportReader

import configurations.ApplicationResources
import configurations.types.ConfigType.{Postgres, Blaze}
import configurations.types.DatabaseConfig
import configurations.types.ServerConfig
import helpers.ConfigLoader

trait ApplicationResourceModule {

  final private[modules] def appResources[F[_]: Async]: Resource[F, ApplicationResources] =
    (
      ConfigLoader[F].load[DatabaseConfig, Postgres.type],
      ConfigLoader[F].load[ServerConfig, Blaze.type]
    ).parMapN(ApplicationResources)

}
