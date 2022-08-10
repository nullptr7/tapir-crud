package com.github.nullptr7
package entrypoint
package modules

import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server

import cats.effect.kernel.{Async, Resource}
import cats.effect.std

import fs2.io.net.Network
import natchez.Trace

import configurations.types.ServerConfig

abstract class BlazeServerModule[F[_]: Async: std.Console: Network: Trace] extends RoutingModule[F] with ApplicationResourceModule {

  final protected[entrypoint] lazy val server: Resource[F, Server] =
    for {
      app    <- appResources
      routes <- withRoutes(app.databaseConfig)
      serve  <- withServer(routes, app.serverConfig)
    } yield serve

  final private[this] def withServer(routes: HttpApp[F], serverConfig: ServerConfig): Resource[F, Server] =
    BlazeServerBuilder[F]
      .bindHttp(serverConfig.port.value, serverConfig.host.value)
      .withHttpApp(routes)
      .resource

}
