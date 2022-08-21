package com.github.nullptr7
package entrypoint

import org.http4s.client.Client

import cats.effect.IOApp
import cats.effect.kernel.Resource

import client.module.BlazeClientModule
import client.{ApiClients, TransportServiceClient}
import configurations.types.{ClientConfig, TransportApiClientDetails}
import modules.{ApplicationResourceModule, BlazeServerModuleV2, RepositoryModule, ServiceLogicModule}
import storage.DatabaseSession

object Startup extends IOApp.Simple with ApplicationResourceModule {

  import cats.effect.IO
  import natchez.Trace.Implicits.noop
  import org.typelevel.log4cats.Logger
  import org.typelevel.log4cats.slf4j.Slf4jLogger

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = {
    val app = for {
      res        <- appResources[IO]
      session    <- DatabaseSession[IO].make(res.databaseConfig)
      repo       <- RepositoryModule.make[IO](session)
      httpClient <- BlazeClientModule[IO].make(res.clientConfig)
      clients    <- initializeClient[IO](res.clientConfig, httpClient)
      logic      <- new ServiceLogicModule[IO](repo, clients).make
      routes     <- Routes.make[IO](logic)
      server     <- BlazeServerModuleV2.make[IO](res.serverConfig)
      serve      <- server.serve(routes)
    } yield serve
    app.useForever
  }

  def initializeClient[F[_]](app: ClientConfig, client: Client[F]): Resource[F, ApiClients[F]] =
    Resource
      .pure(
        ApiClients[F](
          new TransportServiceClient[F](
            client,
            TransportApiClientDetails(
              app.transport.url,
              app.transport.username,
              app.transport.password
            )
          )
        )
      )

}
