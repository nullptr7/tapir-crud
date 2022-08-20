package com.github.nullptr7
package entrypoint
package modules

import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server

import cats.effect.Resource
import cats.effect.kernel.Async

import configurations.types.ServerConfig

abstract class BlazeServerModuleV2[F[_]: Async](serverConfig: ServerConfig) {

  final def serve(route: HttpApp[F]): Resource[F, Server] =
    BlazeServerBuilder[F]
      .withHttpApp(route)
      .bindHttp(serverConfig.port.value, serverConfig.host.value)
      .resource

}

object BlazeServerModuleV2 {

  def make[F[_]: Async](serverConfig: ServerConfig): Resource[F, BlazeServerModuleV2[F]] =
    Resource.pure(
      new BlazeServerModuleV2[F](serverConfig) {}
    )

}
