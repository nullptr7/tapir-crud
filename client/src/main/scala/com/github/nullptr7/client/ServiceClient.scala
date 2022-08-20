package com.github.nullptr7
package client

import org.http4s._
import org.http4s.client.Client
import org.typelevel.ci.CIString
import org.typelevel.log4cats.Logger

import cats.effect.kernel.Async
import cats.implicits._

import configurations.types.ClientDetails
import exception.ServiceClientException

trait ServiceClient[F[_]] {

  protected[client] val client: Client[F]

  protected[client] val clientDetails: ClientDetails

  // We can do unsafe here as we know this is already accepted by java.net.URI
  final def sendAndReceive[Req, Res](
    body:             Option[Req]
  )(implicit encoder: EntityEncoder[F, Req], decoder: EntityDecoder[F, Res], async: Async[F], logger: Logger[F]): F[Res] = {
    val request: Request[F] = body match {
      case None       =>
        Request[F](Method.GET, Uri.unsafeFromString(clientDetails.url.toString))
          .withHeaders(Header.Raw(CIString("Authorization"), s"Bearer ${clientDetails.password.toString + clientDetails.password}"))
      case Some(body) =>
        Request[F](
          method  = Method.POST,
          uri     = Uri.unsafeFromString(clientDetails.url.toString),
          headers = Headers(Header.Raw(CIString("x-mock-match-request-body"), "true"))
        ).withEntity(body)
    }

    client
      .run(request)
      .use(_.as[Res])
      .handleErrorWith { t: Throwable =>
        logger.error(t)("Service called failed") *> async.raiseError(ServiceClientException(t.getMessage))
      }

  }

}
