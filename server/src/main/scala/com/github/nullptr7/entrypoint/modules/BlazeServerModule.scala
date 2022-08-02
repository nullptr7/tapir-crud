package com.github.nullptr7
package entrypoint
package modules

import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server

import cats.effect.kernel.{Async, Resource}
import cats.effect.std
import cats.implicits._

import pureconfig.generic.auto.exportReader

import fs2.io.net.Network
import natchez.Trace

import configurations.types.ServerConfig
import configurations.ApplicationResources
import configurations.types.DatabaseConfig
import helpers.ConfigLoader._
import configurations._
import helpers.ConfigLoader
import types.ConfigType._

abstract class BlazeServerModule[F[_]: Async: std.Console: Network: Trace]
    extends RoutingModule[F] {

  final protected[entrypoint] lazy val server: Resource[F, Server] =
    for {
      res <- loadResource
      routes <- withRoutes(res.databaseConfig)
      serve <- withServer(routes, res.serverConfig)
    } yield serve

  final private[this] def withServer(
      routes: HttpApp[F],
      serverConfig: ServerConfig): Resource[F, Server] =
    BlazeServerBuilder[F]
      .bindHttp(serverConfig.port.value, serverConfig.host.value)
      .withHttpApp(routes)
      .resource

  final private[this] lazy val loadResource: Resource[F, ApplicationResources] =
    (
      ConfigLoader[F].load[DatabaseConfig, Postgres.type],
      ConfigLoader[F].load[ServerConfig, Blaze.type]
    ).parMapN(ApplicationResources)

}
