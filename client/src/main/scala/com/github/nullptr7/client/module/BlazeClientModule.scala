package com.github.nullptr7
package client
package module

import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import cats.effect.kernel.{Async, Resource}

import configurations.types.ClientConfig

sealed abstract class BlazeClientModule[F[_]] {

  def make(clientConfig: ClientConfig): Resource[F, Client[F]]
}

object BlazeClientModule {

  def apply[F[_]: BlazeClientModule]: BlazeClientModule[F] = implicitly

  implicit def clientForAsync[F[_]: Async]: BlazeClientModule[F] =
    new BlazeClientModule[F] {

      override def make(clientConfig: ClientConfig): Resource[F, Client[F]] =
        BlazeClientBuilder[F]
          .withConnectTimeout(clientConfig.timeout)
          .resource

    }

}
