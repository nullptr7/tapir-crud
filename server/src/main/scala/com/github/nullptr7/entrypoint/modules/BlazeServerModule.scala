package com.github.nullptr7
package entrypoint
package modules

import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server

import cats.effect.Resource
import cats.effect.kernel.Async

import configurations.types.ServerConfig

abstract class BlazeServerModule[F[_]: Async](serverConfig: ServerConfig) {

  final def serve(route: HttpApp[F]): Resource[F, Server] =
    BlazeServerBuilder[F]
      .withHttpApp(route)
      .bindHttp(serverConfig.port.value, serverConfig.host.value)
      .resource

}

object BlazeServerModule {

  def make[F[_]: Async](serverConfig: ServerConfig): Resource[F, BlazeServerModule[F]] =
    Resource.pure(
      new BlazeServerModule[F](serverConfig) {}
    )

}
